package com.energiefixers.backend.visit.dto;

import lombok.Getter;
import lombok.Setter;
import com.energiefixers.backend.visit.models.FixVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class FixVisitResponse {
    private Long id;
    private Long propertyId;
    private LocalDate visitDate;
    private String notes;
    private BigDecimal totalMaterialCost;
    private List<InstalledMaterialResponse> installedMaterials;

    public static FixVisitResponse from(FixVisit fixVisit) {
        FixVisitResponse response = new FixVisitResponse();
        response.setId(fixVisit.getId());
        response.setPropertyId(fixVisit.getProperty().getId());
        response.setVisitDate(fixVisit.getVisitDate());
        response.setNotes(fixVisit.getNotes());
        response.setTotalMaterialCost(fixVisit.getTotalMaterialCost());
        response.setInstalledMaterials(
            fixVisit.getInstalledMaterials().stream()
                .map(InstalledMaterialResponse::from)
                .collect(Collectors.toList())
        );
        return response;
    }
}
