package com.energiefixers.backend.visit.dto;

import lombok.Getter;
import lombok.Setter;
import com.energiefixers.backend.visit.models.Material;
import com.energiefixers.backend.visit.models.Material.Category;
import com.energiefixers.backend.visit.models.Material.Unit;

import java.math.BigDecimal;

@Getter
@Setter
public class MaterialResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal priceEuros;
    private BigDecimal estimatedGasSavingM3;
    private BigDecimal estimatedElectricitySavingKwh;
    private Category category;
    private Unit unit;

    public static MaterialResponse from(Material material) {
        MaterialResponse response = new MaterialResponse();
        response.setId(material.getId());
        response.setName(material.getName());
        response.setDescription(material.getDescription());
        response.setPriceEuros(material.getPriceEuros());
        response.setEstimatedGasSavingM3(material.getEstimatedGasSavingM3());
        response.setEstimatedElectricitySavingKwh(material.getEstimatedElectricitySavingKwh());
        response.setCategory(material.getCategory());
        response.setUnit(material.getUnit());
        return response;
    }
}
