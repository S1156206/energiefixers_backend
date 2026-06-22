package com.energiefixers.backend.dashboard.service;

import com.energiefixers.backend.dashboard.dto.*;
import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.repository.EnergyReadingRepository;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.models.Region;
import com.energiefixers.backend.property.models.FixRound;
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
    private static final BigDecimal GAS_PRICE_PER_M3 = new BigDecimal("1.10");
    private static final BigDecimal ELECTRICITY_PRICE_PER_KWH = new BigDecimal("0.30");

    private static final BigDecimal CO2_PER_M3_GAS = new BigDecimal("1.884");
    private static final BigDecimal CO2_PER_KWH_ELEC = new BigDecimal("0.356");

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

    public DashboardCombinedSavingsResponse getCombinedSavings(Long regionId) {
        List<Region> regions = regionId != null
                ? regionRepository.findById(regionId).map(List::of).orElse(List.of())
                : regionRepository.findAll();

        List<RegionCombinedEntry> regionEntries = new ArrayList<>();
        BigDecimal totalActual = BigDecimal.ZERO;
        BigDecimal totalEstimated = BigDecimal.ZERO;

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

            long regionActualCount = 0;
            BigDecimal regionActual = BigDecimal.ZERO;
            long regionEstimatedCount = 0;
            BigDecimal regionEstimated = BigDecimal.ZERO;

            for (Long propId : idsWithVisits) {
                List<FixVisit> visits = visitsByProperty.get(propId);
                List<EnergyReading> readings = readingsByProperty.getOrDefault(propId, List.of());

                PropertySavings savings = computeNormalizedSavings(readings, visits);
                if (savings != null) {
                    regionActualCount++;
                    regionActual = regionActual.add(savings.actualSavingsEuros);
                } else {
                    BigDecimal estimated = computeEstimatedSavingsEuros(visits);
                    if (estimated.compareTo(BigDecimal.ZERO) > 0) {
                        regionEstimatedCount++;
                        regionEstimated = regionEstimated.add(estimated);
                    }
                }
            }

            if (regionActualCount == 0 && regionEstimatedCount == 0) continue;

            BigDecimal combined = regionActual.add(regionEstimated).setScale(2, RoundingMode.HALF_UP);
            regionEntries.add(new RegionCombinedEntry(
                    region.getId(),
                    region.getName(),
                    regionActualCount,
                    regionActual.setScale(2, RoundingMode.HALF_UP),
                    regionEstimatedCount,
                    regionEstimated.setScale(2, RoundingMode.HALF_UP),
                    combined
            ));

            totalActual = totalActual.add(regionActual);
            totalEstimated = totalEstimated.add(regionEstimated);
        }

        regionEntries.sort(Comparator.comparing(RegionCombinedEntry::getRegionName));

        return new DashboardCombinedSavingsResponse(
                totalActual.setScale(2, RoundingMode.HALF_UP),
                totalEstimated.setScale(2, RoundingMode.HALF_UP),
                totalActual.add(totalEstimated).setScale(2, RoundingMode.HALF_UP),
                regionEntries
        );
    }

    private BigDecimal computeEstimatedSavingsEuros(List<FixVisit> visits) {
        BigDecimal total = BigDecimal.ZERO;
        for (FixVisit visit : visits) {
            if (visit.getInstalledMaterials() == null) continue;
            for (var im : visit.getInstalledMaterials()) {
                var mat = im.getMaterial();
                if (mat == null) continue;
                BigDecimal qty = BigDecimal.valueOf(im.getQuantity());
                if (mat.getEstimatedGasSavingM3() != null) {
                    total = total.add(qty.multiply(mat.getEstimatedGasSavingM3()).multiply(GAS_PRICE_PER_M3));
                }
                if (mat.getEstimatedElectricitySavingKwh() != null) {
                    total = total.add(qty.multiply(mat.getEstimatedElectricitySavingKwh()).multiply(ELECTRICITY_PRICE_PER_KWH));
                }
            }
        }
        return total;
    }

    public List<MaterialInstallationSummary> getMaterialsSummary(Long fixRoundId) {
        return fixVisitRepository.sumInstalledQuantityByMaterialFiltered(null, fixRoundId).stream()
                .map(row -> new MaterialInstallationSummary(
                        (String) row[0],
                        row[1].toString(),
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public PublicOverviewResponse getPublicOverview() {
        List<FixVisit> allVisits = fixVisitRepository.findAll();

        Map<FixRound, List<FixVisit>> visitsByRound = allVisits.stream()
                .filter(v -> v.getProperty() != null && v.getProperty().getFixRound() != null)
                .collect(Collectors.groupingBy(v -> v.getProperty().getFixRound()));

        long totalPropertiesHelped = allVisits.stream()
                .map(v -> v.getProperty().getId())
                .distinct()
                .count();

        long totalFixRounds = visitsByRound.keySet().size();

        BigDecimal overallGas = BigDecimal.ZERO;
        BigDecimal overallElec = BigDecimal.ZERO;
        BigDecimal overallEuros = BigDecimal.ZERO;

        List<FixRoundChartEntry> chartData = new ArrayList<>();

        for (Map.Entry<FixRound, List<FixVisit>> entry : visitsByRound.entrySet()) {
            FixRound round = entry.getKey();
            List<FixVisit> roundVisits = entry.getValue();

            long roundProperties = roundVisits.stream()
                    .map(v -> v.getProperty().getId())
                    .distinct()
                    .count();

            BigDecimal roundGas = BigDecimal.ZERO;
            BigDecimal roundElec = BigDecimal.ZERO;
            BigDecimal roundEuros = BigDecimal.ZERO;

            for (FixVisit v : roundVisits) {
                if (v.getInstalledMaterials() == null) continue;
                for (var im : v.getInstalledMaterials()) {
                    var mat = im.getMaterial();
                    if (mat == null) continue;

                    BigDecimal qty = BigDecimal.valueOf(im.getQuantity());

                    if (mat.getEstimatedGasSavingM3() != null) {
                        BigDecimal gas = qty.multiply(mat.getEstimatedGasSavingM3());
                        roundGas = roundGas.add(gas);
                        roundEuros = roundEuros.add(gas.multiply(GAS_PRICE_PER_M3));
                    }
                    if (mat.getEstimatedElectricitySavingKwh() != null) {
                        BigDecimal elec = qty.multiply(mat.getEstimatedElectricitySavingKwh());
                        roundElec = roundElec.add(elec);
                        roundEuros = roundEuros.add(elec.multiply(ELECTRICITY_PRICE_PER_KWH));
                    }
                }
            }

            BigDecimal roundCo2 = roundGas.multiply(CO2_PER_M3_GAS).add(roundElec.multiply(CO2_PER_KWH_ELEC));

            overallGas = overallGas.add(roundGas);
            overallElec = overallElec.add(roundElec);
            overallEuros = overallEuros.add(roundEuros);

            chartData.add(new FixRoundChartEntry(
                    round.getId(),
                    round.getName(),
                    roundCo2.setScale(0, RoundingMode.HALF_UP),
                    roundEuros.setScale(2, RoundingMode.HALF_UP),
                    roundProperties
            ));
        }

        BigDecimal overallCo2 = overallGas.multiply(CO2_PER_M3_GAS).add(overallElec.multiply(CO2_PER_KWH_ELEC));

        chartData.sort(Comparator.comparing(FixRoundChartEntry::getFixRoundName));

        return new PublicOverviewResponse(
                totalFixRounds,
                totalPropertiesHelped,
                overallEuros.setScale(2, RoundingMode.HALF_UP),
                overallGas.setScale(2, RoundingMode.HALF_UP),
                overallElec.setScale(2, RoundingMode.HALF_UP),
                overallCo2.setScale(0, RoundingMode.HALF_UP),
                chartData
        );
    }

    private record PropertySavings(BigDecimal monthlyActualSavingsEuros, BigDecimal actualSavingsEuros) {}
}