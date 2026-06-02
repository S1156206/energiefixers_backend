package com.energiefixers.backend.admin.service;

import com.energiefixers.backend.admin.dto.*;
import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.repository.EnergyReadingRepository;
import com.energiefixers.backend.visit.models.FixVisit;
import com.energiefixers.backend.visit.repository.FixVisitRepository;
import com.energiefixers.backend.visit.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final FixVisitRepository fixVisitRepository;
    private final MaterialRepository materialRepository;
    private final EnergyReadingRepository energyReadingRepository;

    public SavingsSummaryResponse getSummary(LocalDate from, LocalDate to) {
        List<PropertySavingsEntry> entries = getSavingsByProperty(null, from, to);

        long totalVisits = entries.stream().mapToLong(PropertySavingsEntry::getVisitsCount).sum();
        BigDecimal totalMaterialCost = sumField(entries, PropertySavingsEntry::getTotalMaterialCostEuros);
        BigDecimal estimatedGas = sumField(entries, PropertySavingsEntry::getEstimatedGasSavingsM3);
        BigDecimal estimatedElec = sumField(entries, PropertySavingsEntry::getEstimatedElectricitySavingsKwh);

        long withActualData = entries.stream()
                .filter(e -> e.getActualGasSavingsM3() != null)
                .count();
        BigDecimal actualGas = sumNullableField(entries, PropertySavingsEntry::getActualGasSavingsM3);
        BigDecimal actualElec = sumNullableField(entries, PropertySavingsEntry::getActualElectricitySavingsKwh);
        BigDecimal actualCost = sumNullableField(entries, PropertySavingsEntry::getActualCostSavingsEuros);

        return new SavingsSummaryResponse(
                entries.size(), totalVisits, totalMaterialCost,
                estimatedGas, estimatedElec,
                actualGas, actualElec, actualCost, withActualData
        );
    }

    public List<RegionStatsResponse> getSavingsByRegion(Long regionId, LocalDate from, LocalDate to) {
        List<PropertySavingsEntry> entries = getSavingsByProperty(regionId, from, to);

        Map<Long, List<PropertySavingsEntry>> byRegion = entries.stream()
                .collect(Collectors.groupingBy(e -> e.getRegionId() != null ? e.getRegionId() : 0L));

        return byRegion.entrySet().stream()
                .map(e -> {
                    List<PropertySavingsEntry> group = e.getValue();
                    Long rId = e.getKey() == 0L ? null : e.getKey();
                    String regionName = group.get(0).getRegionName();

                    long visitsCount = group.stream().mapToLong(PropertySavingsEntry::getVisitsCount).sum();
                    BigDecimal materialCost = sumField(group, PropertySavingsEntry::getTotalMaterialCostEuros);
                    BigDecimal estGas = sumField(group, PropertySavingsEntry::getEstimatedGasSavingsM3);
                    BigDecimal estElec = sumField(group, PropertySavingsEntry::getEstimatedElectricitySavingsKwh);
                    long withActual = group.stream().filter(p -> p.getActualGasSavingsM3() != null).count();
                    BigDecimal actGas = sumNullableField(group, PropertySavingsEntry::getActualGasSavingsM3);
                    BigDecimal actElec = sumNullableField(group, PropertySavingsEntry::getActualElectricitySavingsKwh);
                    BigDecimal actCost = sumNullableField(group, PropertySavingsEntry::getActualCostSavingsEuros);

                    return new RegionStatsResponse(
                            rId, regionName, (long) group.size(), visitsCount,
                            estGas, estElec, materialCost,
                            actGas, actElec, actCost, withActual
                    );
                })
                .sorted(Comparator.comparing(RegionStatsResponse::getRegionName))
                .collect(Collectors.toList());
    }

    public List<PropertySavingsEntry> getSavingsByProperty(Long regionId, LocalDate from, LocalDate to) {
        List<Object[]> rows = fixVisitRepository.getSavingsByProperty(regionId, from, to);

        List<Long> propertyIds = rows.stream()
                .map(r -> (Long) r[0])
                .collect(Collectors.toList());

        if (propertyIds.isEmpty()) return List.of();

        Map<Long, List<EnergyReading>> readingsByProperty = energyReadingRepository
                .findAllByPropertyIdIn(propertyIds).stream()
                .collect(Collectors.groupingBy(er -> er.getProperty().getId()));

        Map<Long, List<FixVisit>> visitsByProperty = fixVisitRepository
                .findAllByPropertyIdIn(propertyIds).stream()
                .collect(Collectors.groupingBy(fv -> fv.getProperty().getId()));

        return rows.stream().map(row -> {
            Long propertyId = (Long) row[0];
            List<EnergyReading> readings = readingsByProperty.getOrDefault(propertyId, List.of());
            List<FixVisit> visits = visitsByProperty.getOrDefault(propertyId, List.of());

            ActualSavings actual = computeActualSavings(readings, visits);

            return new PropertySavingsEntry(
                    propertyId,
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    toLong(row[4]),
                    (String) row[5],
                    (String) row[6],
                    (String) row[7],
                    toLong(row[8]),
                    toBigDecimal(row[9]),
                    toBigDecimal(row[10]),
                    toBigDecimal(row[11]),
                    actual.gasSavingsM3,
                    actual.electricitySavingsKwh,
                    actual.costSavingsEuros
            );
        }).collect(Collectors.toList());
    }

    public MaterialSummaryResponse getMaterials(Long regionId, Long propertyId, LocalDate from, LocalDate to) {
        List<Object[]> rows = materialRepository.getMaterialUsageStatsFiltered(regionId, propertyId, from, to);

        List<MaterialUsageStatsResponse> items = rows.stream()
                .map(row -> new MaterialUsageStatsResponse(
                        (Long) row[0],
                        (String) row[1],
                        row[2] != null ? row[2].toString() : null,
                        toLong(row[3]),
                        toBigDecimal(row[4])
                ))
                .collect(Collectors.toList());

        BigDecimal totalCost = items.stream()
                .map(MaterialUsageStatsResponse::getTotalCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalQuantity = items.stream()
                .mapToLong(MaterialUsageStatsResponse::getTotalQuantityUsed)
                .sum();

        return new MaterialSummaryResponse(totalCost, totalQuantity, items);
    }

    private ActualSavings computeActualSavings(List<EnergyReading> readings, List<FixVisit> visits) {
        if (visits.isEmpty() || readings.size() < 2) return ActualSavings.empty();

        LocalDate firstVisit = visits.stream()
                .map(FixVisit::getVisitDate)
                .min(LocalDate::compareTo)
                .orElse(null);
        LocalDate lastVisit = visits.stream()
                .map(FixVisit::getVisitDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        if (firstVisit == null) return ActualSavings.empty();

        Optional<EnergyReading> baseline = readings.stream()
                .filter(r -> r.getPeriodEnd().isBefore(firstVisit))
                .max(Comparator.comparing(EnergyReading::getPeriodEnd));

        Optional<EnergyReading> postVisit = readings.stream()
                .filter(r -> r.getPeriodStart().isAfter(lastVisit))
                .max(Comparator.comparing(EnergyReading::getPeriodEnd));

        if (baseline.isEmpty() || postVisit.isEmpty()) return ActualSavings.empty();

        EnergyReading before = baseline.get();
        EnergyReading after = postVisit.get();

        return new ActualSavings(
                subtract(before.getGasUsageM3(), after.getGasUsageM3()),
                subtract(before.getElectricityUsageKwh(), after.getElectricityUsageKwh()),
                subtract(before.getTotalCostEuros(), after.getTotalCostEuros())
        );
    }

    private BigDecimal sumField(List<PropertySavingsEntry> entries,
                                java.util.function.Function<PropertySavingsEntry, BigDecimal> getter) {
        return entries.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumNullableField(List<PropertySavingsEntry> entries,
                                        java.util.function.Function<PropertySavingsEntry, BigDecimal> getter) {
        List<BigDecimal> values = entries.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (values.isEmpty()) return null;
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal subtract(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return null;
        return a.subtract(b);
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        return ((Number) value).longValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }

    private record ActualSavings(BigDecimal gasSavingsM3, BigDecimal electricitySavingsKwh, BigDecimal costSavingsEuros) {
        static ActualSavings empty() { return new ActualSavings(null, null, null); }
    }
}
