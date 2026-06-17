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
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnergyReadingService {

    private final EnergyReadingRepository energyReadingRepository;
    private final UserRepository userRepository;
    private final FixVisitRepository fixVisitRepository;

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

        List<EnergyReading> readings = energyReadingRepository.findAllByPropertyIdOrderByPeriodEndAsc(propertyId);

        Optional<EnergyReading> baseline = readings.stream()
                .filter(r -> r.getPeriodEnd().isBefore(firstVisit))
                .max(Comparator.comparing(EnergyReading::getPeriodEnd));

        Optional<EnergyReading> postVisit = readings.stream()
                .filter(r -> r.getPeriodStart().isAfter(lastVisit))
                .max(Comparator.comparing(EnergyReading::getPeriodEnd));

        if (baseline.isEmpty() || postVisit.isEmpty()) {
            return new TenantSavingsResponse(firstVisit, null, null, null, null, null, null, null, false);
        }

        EnergyReading before = baseline.get();
        EnergyReading after = postVisit.get();

        return new TenantSavingsResponse(
                firstVisit,
                subtract(before.getGasUsageM3(), after.getGasUsageM3()),
                subtract(before.getElectricityUsageKwh(), after.getElectricityUsageKwh()),
                subtract(before.getTotalCostEuros(), after.getTotalCostEuros()),
                before.getPeriodStart(),
                before.getPeriodEnd(),
                after.getPeriodStart(),
                after.getPeriodEnd(),
                true
        );
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
}
