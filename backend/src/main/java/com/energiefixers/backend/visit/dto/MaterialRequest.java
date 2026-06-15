package com.energiefixers.backend.visit.dto;

import com.energiefixers.backend.visit.models.Material;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MaterialRequest {
    private String name;
    private String description;
    private BigDecimal priceEuros;
    private BigDecimal estimatedGasSavingM3;
    private BigDecimal estimatedElectricitySavingKwh;
    private Material.Category category;
}
