package com.energiefixers.backend.reports.service;

import com.energiefixers.backend.reports.dto.DashboardMaterialRowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.repository.PropertyRepository;
import com.energiefixers.backend.visit.models.FixVisit;
import com.energiefixers.backend.visit.models.InstalledMaterial;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PropertyRepository propertyRepository;

    @Transactional(readOnly = true)
    public List<DashboardMaterialRowResponse> getInstalledMaterialsReport() {
        List<Property> properties = propertyRepository.findAll();
        List<DashboardMaterialRowResponse> reportList = new ArrayList<>();

        for (Property property : properties) {
            String address = property.getStreet() + " " + property.getHouseNumber();
            if (property.getHouseNumberSuffix() != null && !property.getHouseNumberSuffix().isBlank()) {
                address += property.getHouseNumberSuffix();
            }

            Long regionId = property.getRegion() != null ? property.getRegion().getId() : null;
            String regionName = property.getRegion() != null ? property.getRegion().getName() : "Onbekende regio";

            Long fixRoundId = property.getFixRound() != null ? property.getFixRound().getId() : null;
            String fixRoundName = property.getFixRound() != null ? property.getFixRound().getName() : null;

            if (property.getFixVisits() != null) {
                for (FixVisit visit : property.getFixVisits()) {

                    if (visit.getInstalledMaterials() != null) {
                        for (InstalledMaterial installed : visit.getInstalledMaterials()) {

                            String matName = installed.getMaterial() != null ? installed.getMaterial().getName() : "Onbekend";

                            double price = installed.getMaterial() != null && installed.getMaterial().getPriceEuros() != null
                                    ? installed.getMaterial().getPriceEuros().doubleValue()
                                    : 0.0;

                            double totalCost = installed.getQuantity() * price;

                            DashboardMaterialRowResponse row = DashboardMaterialRowResponse.builder()
                                    .propertyId(property.getId())
                                    .address(address)
                                    .regionId(regionId)
                                    .regionName(regionName)
                                    .fixRoundId(fixRoundId)
                                    .fixRoundName(fixRoundName)
                                    .visitDate(visit.getVisitDate() != null ? visit.getVisitDate().toString() : "")
                                    .materialName(matName)
                                    .quantity(installed.getQuantity())
                                    .cost(totalCost)
                                    .build();

                            reportList.add(row);
                        }
                    }
                }
            }
        }

        return reportList;
    }
}