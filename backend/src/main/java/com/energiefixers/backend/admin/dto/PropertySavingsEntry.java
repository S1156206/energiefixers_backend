package com.energiefixers.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PropertySavingsEntry {
    private Long propertyId;
    private String street;
    private String houseNumber;
    private String postcode;
    private Long regionId;
    private String regionName;
    private String tenantName;
    private String tenantEmail;
    private long visitsCount;
    private BigDecimal totalMaterialCostEuros;
    private BigDecimal estimatedGasSavingsM3;
    private BigDecimal estimatedElectricitySavingsKwh;
    private BigDecimal actualGasSavingsM3;
    private BigDecimal actualElectricitySavingsKwh;
    private BigDecimal actualCostSavingsEuros;
}
