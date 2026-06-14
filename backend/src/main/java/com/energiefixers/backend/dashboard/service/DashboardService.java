package com.energiefixers.backend.dashboard.service;

import com.energiefixers.backend.dashboard.dto.DashboardSavingsResponse;
import com.energiefixers.backend.dashboard.dto.RegionDashboardEntry;
import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.repository.EnergyReadingRepository;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.models.Region;
import com.energiefixers.backend.property.repository.PropertyRepository;
import com.energiefixers.backend.property.repository.RegionRepository;
import com.energiefixers.backend.visit.models.FixVisit;
import com.energiefixers.backend.visit.repository.FixVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int MIN_PROPERTIES_THRESHOLD = 3;
    private static final double AVG_DAYS_PER_MONTH = 30.4375;

    private final RegionRepository regionRepository;
    private final PropertyRepository propertyRepository;
    private final FixVisitRepository fixVisitRepository;
    private final EnergyReadingRepository energyReadingRepository;

    public DashboardSavingsResponse getSavings(Long regionId) {
        List<Region> regions = regionId != null
                ? regionRepository.findById(regionId).map(List::of).orElse(List.of())
                : regionRepository.findAll();

        List<RegionDashboardEntry> regionEntries = new ArrayList<>();
        long totalPropertiesWithData = 0;
        BigDecimal totalSavings = BigDecimal.ZERO;
        List<BigDecimal> allMonthlySavings = new ArrayList<>();

        for (Region region : regions) {
            List<Property> properties = propertyRepository.findAllByRegionId(region.getId());
            if (properties.isEmpty()) continue;

            List<Long> propertyIds = properties.stream().map(Property::getId).collect(Collectors.toList());

            Map<Long, List<FixVisit>> visitsByProperty = fixVisitRepository
                    .findAllByPropertyIdIn(propertyIds).stream()
                    .collect(Collectors.groupingBy(fv -> fv.getProperty().getId()));

            List<Long> idsWithVisits = propertyIds.stream()
                    .filter(visitsByProperty::containsKey)
                    .collect(Collectors.toList());

            if (idsWithVisits.isEmpty()) continue;

            Map<Long, List<EnergyReading>> readingsByProperty = energyReadingRepository
                    .findAllByPropertyIdIn(idsWithVisits).stream()
                    .collect(Collectors.groupingBy(er -> er.getProperty().getId()));

            long regionPropertiesWithData = 0;
            BigDecimal regionTotalSavings = BigDecimal.ZERO;
            List<BigDecimal> regionMonthlySavings = new ArrayList<>();

            for (Long propId : idsWithVisits) {
                List<FixVisit> visits = visitsByProperty.get(propId);
                List<EnergyReading> readings = readingsByProperty.getOrDefault(propId, List.of());

                PropertySavings savings = computeNormalizedSavings(readings, visits);
                if (savings == null) continue;

                regionPropertiesWithData++;
                regionTotalSavings = regionTotalSavings.add(savings.actualSavingsEuros);
                regionMonthlySavings.add(savings.monthlyActualSavingsEuros);
            }

            if (regionPropertiesWithData < MIN_PROPERTIES_THRESHOLD) continue;

            BigDecimal regionAvgMonthly = average(regionMonthlySavings);
            regionEntries.add(new RegionDashboardEntry(
                    region.getId(),
                    region.getName(),
                    regionPropertiesWithData,
                    regionTotalSavings.setScale(2, RoundingMode.HALF_UP),
                    regionAvgMonthly != null ? regionAvgMonthly.setScale(2, RoundingMode.HALF_UP) : null
            ));

            totalPropertiesWithData += regionPropertiesWithData;
            totalSavings = totalSavings.add(regionTotalSavings);
            allMonthlySavings.addAll(regionMonthlySavings);
        }

        regionEntries.sort(Comparator.comparing(RegionDashboardEntry::getRegionName));

        BigDecimal overallAvgMonthly = average(allMonthlySavings);
        return new DashboardSavingsResponse(
                totalPropertiesWithData,
                totalSavings.setScale(2, RoundingMode.HALF_UP),
                overallAvgMonthly != null ? overallAvgMonthly.setScale(2, RoundingMode.HALF_UP) : null,
                regionEntries
        );
    }

    /**
     * Normalises energy readings to a monthly cost average before and after the fix visit,
     * then computes the actual savings accrued in the post-visit period.
     *
     * Returns null when there are insufficient readings to make the comparison.
     */
    private PropertySavings computeNormalizedSavings(List<EnergyReading> readings, List<FixVisit> visits) {
        if (visits.isEmpty() || readings.isEmpty()) return null;

        LocalDate firstVisit = visits.stream()
                .map(FixVisit::getVisitDate)
                .min(LocalDate::compareTo)
                .orElse(null);
        LocalDate lastVisit = visits.stream()
                .map(FixVisit::getVisitDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        if (firstVisit == null) return null;

        List<EnergyReading> beforeReadings = readings.stream()
                .filter(r -> r.getTotalCostEuros() != null && r.getPeriodEnd().compareTo(firstVisit) <= 0)
                .collect(Collectors.toList());

        List<EnergyReading> afterReadings = readings.stream()
                .filter(r -> r.getTotalCostEuros() != null && r.getPeriodStart().compareTo(lastVisit) >= 0)
                .collect(Collectors.toList());

        if (beforeReadings.isEmpty() || afterReadings.isEmpty()) return null;

        double beforeMonths = totalMonths(beforeReadings);
        double afterMonths = totalMonths(afterReadings);

        if (beforeMonths <= 0 || afterMonths <= 0) return null;

        BigDecimal totalBeforeCost = beforeReadings.stream()
                .map(EnergyReading::getTotalCostEuros)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAfterCost = afterReadings.stream()
                .map(EnergyReading::getTotalCostEuros)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal beforeMonthly = totalBeforeCost.divide(
                BigDecimal.valueOf(beforeMonths), 6, RoundingMode.HALF_UP);
        BigDecimal afterMonthly = totalAfterCost.divide(
                BigDecimal.valueOf(afterMonths), 6, RoundingMode.HALF_UP);

        BigDecimal monthlyActualSavings = beforeMonthly.subtract(afterMonthly);

        // Actual euros saved in the post-visit period vs what the tenant would have paid
        BigDecimal actualSavingsInAfterPeriod = monthlyActualSavings.multiply(BigDecimal.valueOf(afterMonths));

        return new PropertySavings(monthlyActualSavings, actualSavingsInAfterPeriod);
    }

    private double totalMonths(List<EnergyReading> readings) {
        return readings.stream()
                .mapToDouble(r -> ChronoUnit.DAYS.between(r.getPeriodStart(), r.getPeriodEnd()) / AVG_DAYS_PER_MONTH)
                .sum();
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) return null;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
    }

    private record PropertySavings(BigDecimal monthlyActualSavingsEuros, BigDecimal actualSavingsEuros) {}
}
