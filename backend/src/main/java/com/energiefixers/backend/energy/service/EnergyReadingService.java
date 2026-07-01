package com.energiefixers.backend.energy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.energiefixers.backend.energy.dto.EnergyReadingRequest;
import com.energiefixers.backend.energy.dto.TenantSavingsResponse;
import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.repository.EnergyReadingRepository;
import com.energiefixers.backend.shared.NotFoundException;
import com.energiefixers.backend.user.models.User;
import com.energiefixers.backend.user.repository.UserRepository;
import com.energiefixers.backend.visit.models.FixVisit;
import com.energiefixers.backend.visit.repository.FixVisitRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnergyReadingService {

    private final EnergyReadingRepository energyReadingRepository;
    private final UserRepository userRepository;
    private final FixVisitRepository fixVisitRepository;
    private final SavingsCalculator savingsCalculator;

    public List<EnergyReading> findForTenant(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getProperty() == null) {
            throw new IllegalStateException("Current user is not assigned to a property.");
        }

        return energyReadingRepository.findAllByPropertyId(user.getProperty().getId());
    }

    @Transactional
    public EnergyReading createForTenant(Long userId, EnergyReadingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getProperty() == null) {
            throw new IllegalStateException("Current user is not assigned to a property.");
        }

        validatePeriod(request.getPeriodStart(), request.getPeriodEnd());
        validateNoOverlap(user.getProperty().getId(), request.getPeriodStart(), request.getPeriodEnd(), null);

        EnergyReading reading = new EnergyReading();
        reading.setProperty(user.getProperty());
        reading.setPeriodStart(request.getPeriodStart());
        reading.setPeriodEnd(request.getPeriodEnd());
        reading.setGasUsageM3(request.getGasUsageM3());
        reading.setElectricityUsageKwh(request.getElectricityUsageKwh());
        reading.setTotalCostEuros(request.getTotalCostEuros());
        reading.setSourceType(EnergyReading.SourceType.ANNUAL_BILL_MANUAL);

        return energyReadingRepository.save(reading);
    }

    @Transactional
    public EnergyReading updateForTenant(Long userId, Long readingId, EnergyReadingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getProperty() == null) {
            throw new IllegalStateException("Current user is not assigned to a property.");
        }

        EnergyReading reading = energyReadingRepository.findById(readingId)
                .orElseThrow(() -> new NotFoundException("Energy reading not found: " + readingId));

        if (!reading.getProperty().getId().equals(user.getProperty().getId())) {
            throw new IllegalStateException("You do not have permission to update this reading.");
        }

        validatePeriod(request.getPeriodStart(), request.getPeriodEnd());
        validateNoOverlap(user.getProperty().getId(), request.getPeriodStart(), request.getPeriodEnd(), readingId);

        reading.setPeriodStart(request.getPeriodStart());
        reading.setPeriodEnd(request.getPeriodEnd());
        reading.setGasUsageM3(request.getGasUsageM3());
        reading.setElectricityUsageKwh(request.getElectricityUsageKwh());
        reading.setTotalCostEuros(request.getTotalCostEuros());

        return energyReadingRepository.save(reading);
    }

    @Transactional
    public void deleteForTenant(Long userId, Long readingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getProperty() == null) {
            throw new IllegalStateException("Current user is not assigned to a property.");
        }

        EnergyReading reading = energyReadingRepository.findById(readingId)
                .orElseThrow(() -> new NotFoundException("Energy reading not found: " + readingId));

        if (!reading.getProperty().getId().equals(user.getProperty().getId())) {
            throw new IllegalStateException("You do not have permission to delete this reading.");
        }

        energyReadingRepository.deleteById(readingId);
    }

    public TenantSavingsResponse getSavingsForTenant(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getProperty() == null) {
            throw new IllegalStateException("Current user is not assigned to a property.");
        }

        Long propertyId = user.getProperty().getId();

        List<FixVisit> visits = fixVisitRepository.findAllByPropertyId(propertyId);
        if (visits.isEmpty()) {
            throw new NotFoundException("No fix visit found for this property.");
        }

        LocalDate firstVisit = visits.stream()
                .map(FixVisit::getVisitDate)
                .min(LocalDate::compareTo)
                .orElseThrow();
        LocalDate lastVisit = visits.stream()
                .map(FixVisit::getVisitDate)
                .max(LocalDate::compareTo)
                .orElseThrow();

        // Material-based estimates — always available
        List<Object[]> estRows = fixVisitRepository.sumEstimatedSavingsByPropertyId(propertyId);
        Object[] est = estRows.isEmpty() ? new Object[]{null, null} : estRows.get(0);
        BigDecimal estimatedAnnualGas  = toBigDecimal(est[0]);
        BigDecimal estimatedAnnualElec = toBigDecimal(est[1]);
        long daysSinceLastVisit = ChronoUnit.DAYS.between(lastVisit, LocalDate.now());
        BigDecimal daysFraction = BigDecimal.valueOf(daysSinceLastVisit)
                .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        BigDecimal estimatedToDateGas  = estimatedAnnualGas.multiply(daysFraction).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedToDateElec = estimatedAnnualElec.multiply(daysFraction).setScale(2, RoundingMode.HALF_UP);

        // Split readings into before/after the fix visit window
        List<EnergyReading> readings = energyReadingRepository.findAllByPropertyIdOrderByPeriodEndAsc(propertyId);
        List<EnergyReading> beforeReadings = readings.stream()
                .filter(r -> r.getPeriodEnd().isBefore(firstVisit))
                .toList();
        List<EnergyReading> afterReadings = readings.stream()
                .filter(r -> r.getPeriodStart().isAfter(lastVisit))
                .toList();

        if (beforeReadings.isEmpty() || afterReadings.isEmpty()) {
            return new TenantSavingsResponse(
                    firstVisit, false,
                    estimatedAnnualGas, estimatedAnnualElec, estimatedToDateGas, estimatedToDateElec,
                    null, null, null, null, null, null,
                    beforeReadings.size(), afterReadings.size());
        }

        BigDecimal avgDailyGasBefore  = savingsCalculator.averageDailyRate(beforeReadings, EnergyReading::getGasUsageM3);
        BigDecimal avgDailyElecBefore = savingsCalculator.averageDailyRate(beforeReadings, EnergyReading::getElectricityUsageKwh);
        BigDecimal avgDailyCostBefore = savingsCalculator.averageDailyRate(beforeReadings, EnergyReading::getTotalCostEuros);

        BigDecimal annualGas  = savingsCalculator.annualize(subtract(avgDailyGasBefore,  savingsCalculator.averageDailyRate(afterReadings, EnergyReading::getGasUsageM3)));
        BigDecimal annualElec = savingsCalculator.annualize(subtract(avgDailyElecBefore, savingsCalculator.averageDailyRate(afterReadings, EnergyReading::getElectricityUsageKwh)));
        BigDecimal annualCost = savingsCalculator.annualize(subtract(avgDailyCostBefore, savingsCalculator.averageDailyRate(afterReadings, EnergyReading::getTotalCostEuros)));

        // Clamp to zero: savings can never be negative
        if (annualGas  != null) annualGas  = annualGas.max(BigDecimal.ZERO);
        if (annualElec != null) annualElec = annualElec.max(BigDecimal.ZERO);
        if (annualCost != null) annualCost = annualCost.max(BigDecimal.ZERO);

        // Same formula as estimates: annualRate × (daysSinceFixVisit / 365)
        // This keeps the "to date" counter growing every day, consistent with the estimated track.
        BigDecimal totalGasSaved  = annualGas  != null ? annualGas.multiply(daysFraction).setScale(2, RoundingMode.HALF_UP)  : null;
        BigDecimal totalElecSaved = annualElec != null ? annualElec.multiply(daysFraction).setScale(2, RoundingMode.HALF_UP) : null;
        BigDecimal totalCostSaved = annualCost != null ? annualCost.multiply(daysFraction).setScale(2, RoundingMode.HALF_UP) : null;

        return new TenantSavingsResponse(
                firstVisit, true,
                estimatedAnnualGas, estimatedAnnualElec, estimatedToDateGas, estimatedToDateElec,
                annualGas, annualElec, annualCost,
                totalGasSaved, totalElecSaved, totalCostSaved,
                beforeReadings.size(), afterReadings.size());
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "periodEnd must be after periodStart.");
        }
    }

    private void validateNoOverlap(Long propertyId, LocalDate start, LocalDate end, Long excludeId) {
        List<EnergyReading> conflicts = energyReadingRepository.findOverlapping(propertyId, start, end, excludeId);
        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The period overlaps with an existing energy reading.");
        }
    }

    private BigDecimal subtract(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return null;
        return a.subtract(b);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Double d) {
            if (d.isNaN() || d.isInfinite()) return BigDecimal.ZERO;
            return BigDecimal.valueOf(d);
        }
        if (value instanceof Float f) {
            if (f.isNaN() || f.isInfinite()) return BigDecimal.ZERO;
            return BigDecimal.valueOf(f.doubleValue());
        }
        if (value instanceof Number n) {
            try {
                return new BigDecimal(n.toString());
            } catch (NumberFormatException e) {
                return BigDecimal.valueOf(n.doubleValue());
            }
        }
        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException e) {
            try {
                double d = Double.parseDouble(value.toString().trim());
                return Double.isFinite(d) ? BigDecimal.valueOf(d) : BigDecimal.ZERO;
            } catch (NumberFormatException e2) {
                return BigDecimal.ZERO;
            }
        }
    }
}
