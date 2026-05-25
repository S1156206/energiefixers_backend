package com.energiefixers.backend.visit.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class FixVisitRequest {
    private LocalDate visitDate;
    private String notes;
    private BigDecimal totalMaterialCost;
    private List<InstalledMaterialRequest> installedMaterials;
}
