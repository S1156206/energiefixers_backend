package com.energiefixers.backend.social.service;

import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.repository.EnergyReadingRepository;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.repository.PropertyRepository;
import com.energiefixers.backend.property.service.PropertyService;
import com.energiefixers.backend.social.dto.NeighborSavingsEntry;
import com.energiefixers.backend.social.dto.NeighborhoodSavingsResponse;
import com.energiefixers.backend.shared.NotFoundException;
import com.energiefixers.backend.visit.models.FixVisit;
import com.energiefixers.backend.visit.repository.FixVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NeighborhoodService {

    private static final int MIN_PROPERTIES_THRESHOLD = 3;

    private final PropertyService propertyService;
    private final PropertyRepository propertyRepository;
    private final FixVisitRepository fixVisitRepository;
    private final EnergyReadingRepository energyReadingRepository;

    public NeighborhoodSavingsResponse getNeighborhoodSavings(Long userId) {
        Property myProperty = propertyService.getMyProperty(userId);

        if (myProperty.getRegion() == null) {
            throw new NotFoundException("No region linked to your property.");
        }

        Long regionId = myProperty.getRegion().getId();
        String regionName = myProperty.getRegion().getName();

        List<Property> regionProperties = propertyRepository.findAllByRegionId(regionId);

        List<Long> propertyIds = regionProperties.stream()
                .map(Property::getId)
                .collect(Collectors.toList());

        Map<Long, List<FixVisit>> visitsByProperty = fixVisitRepository
                .findAllByPropertyIdIn(propertyIds).stream()
                .collect(Collectors.groupingBy(fv -> fv.getProperty().getId()));

        List<Long> idsWithVisits = propertyIds.stream()
                .filter(visitsByProperty::containsKey)
                .collect(Collectors.toList());

        if (idsWithVisits.size() < MIN_PROPERTIES_THRESHOLD) {
            return new NeighborhoodSavingsResponse(regionName, 0, List.of());
        }

        Map<Long, List<EnergyReading>> readingsByProperty = energyReadingRepository
                .findAllByPropertyIdIn(idsWithVisits).stream()
                .collect(Collectors.groupingBy(er -> er.getProperty().getId()));

        List<NeighborSavingsEntry> entries = new ArrayList<>();
        for (Long propId : idsWithVisits) {
            List<FixVisit> visits = visitsByProperty.get(propId);
            List<EnergyReading> readings = readingsByProperty.getOrDefault(propId, List.of());

            LocalDate firstVisitDate = visits.stream()
                    .map(FixVisit::getVisitDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);

            BigDecimal estGas = computeEstimatedGasSavings(visits);
            BigDecimal estElec = computeEstimatedElectricitySavings(visits);
            ActualSavings actual = computeActualSavings(readings, visits);

            entries.add(new NeighborSavingsEntry(
                    propId.equals(myProperty.getId()),
                    firstVisitDate,
                    estGas,
                    estElec,
                    actual.gasSavingsM3,
                    actual.electricitySavingsKwh,
                    actual.costSavingsEuros,
                    actual.gasSavingsM3 != null
            ));
        }

        Collections.shuffle(entries);

        return new NeighborhoodSavingsResponse(regionName, entries.size(), entries);
    }

    private BigDecimal computeEstimatedGasSavings(List<FixVisit> visits) {
        return visits.stream()
                .flatMap(fv -> fv.getInstalledMaterials().stream())
                .map(im -> im.getMaterial().getEstimatedGasSavingM3()
                        .multiply(BigDecimal.valueOf(im.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal computeEstimatedElectricitySavings(List<FixVisit> visits) {
        return visits.stream()
                .flatMap(fv -> fv.getInstalledMaterials().stream())
                .map(im -> im.getMaterial().getEstimatedElectricitySavingKwh()
                        .multiply(BigDecimal.valueOf(im.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ActualSavings computeActualSavings(List<EnergyReading> readings, List<FixVisit> visits) {
        if (readings.size() < 2) return ActualSavings.empty();

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

    private BigDecimal subtract(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return null;
        return a.subtract(b);
    }

    private record ActualSavings(BigDecimal gasSavingsM3, BigDecimal electricitySavingsKwh, BigDecimal costSavingsEuros) {
        static ActualSavings empty() { return new ActualSavings(null, null, null); }
    }
}
