package com.energiefixers.backend.energy.dto;

import lombok.Getter;
import lombok.Setter;

import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.visit.models.FixVisit;
import com.energiefixers.backend.visit.models.InstalledMaterial;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class SubmissionInfoResponse {

    private String address;
    private LocalDate visitDate;
    private List<InstalledMaterialSummary> materials;

    @Getter
    @Setter
    public static class InstalledMaterialSummary {
        private String name;
        private String category;
        private int quantity;
        private BigDecimal totalEstimatedGasSavingM3;
        private BigDecimal totalEstimatedElectricitySavingKwh;

        public static InstalledMaterialSummary from(InstalledMaterial im) {
            InstalledMaterialSummary summary = new InstalledMaterialSummary();
            summary.setName(im.getMaterial().getName());
            summary.setCategory(im.getMaterial().getCategory() != null ? im.getMaterial().getCategory().name() : null);
            summary.setQuantity(im.getQuantity());
            if (im.getMaterial().getEstimatedGasSavingM3() != null) {
                summary.setTotalEstimatedGasSavingM3(
                    im.getMaterial().getEstimatedGasSavingM3().multiply(BigDecimal.valueOf(im.getQuantity())));
            }
            if (im.getMaterial().getEstimatedElectricitySavingKwh() != null) {
                summary.setTotalEstimatedElectricitySavingKwh(
                    im.getMaterial().getEstimatedElectricitySavingKwh().multiply(BigDecimal.valueOf(im.getQuantity())));
            }
            return summary;
        }
    }

    public static SubmissionInfoResponse from(Property property) {
        SubmissionInfoResponse response = new SubmissionInfoResponse();
        response.setAddress(buildAddress(property));

        FixVisit latestVisit = property.getFixVisits() == null ? null :
            property.getFixVisits().stream()
                .max(Comparator.comparing(FixVisit::getVisitDate))
                .orElse(null);

        if (latestVisit != null) {
            response.setVisitDate(latestVisit.getVisitDate());
            response.setMaterials(latestVisit.getInstalledMaterials() == null ? List.of() :
                latestVisit.getInstalledMaterials().stream()
                    .map(InstalledMaterialSummary::from)
                    .collect(Collectors.toList()));
        } else {
            response.setMaterials(List.of());
        }

        return response;
    }

    private static String buildAddress(Property p) {
        return p.getStreet() + " " + p.getHouseNumber()
            + (p.getHouseNumberSuffix() != null ? p.getHouseNumberSuffix() : "")
            + ", " + p.getPostcode();
    }
}
