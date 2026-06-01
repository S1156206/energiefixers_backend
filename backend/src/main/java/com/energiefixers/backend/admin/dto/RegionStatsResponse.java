package com.energiefixers.backend.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RegionStatsResponse {
    private Long regionId;
    private String regionName;
    private Long propertiesCount;
    private Long tenantsCount;
    private BigDecimal totalGasSavingsM3;
    private BigDecimal totalElectricitySavingsKwh;
    private BigDecimal totalMaterialCost;

    public RegionStatsResponse(Long regionId, String regionName, Long propertiesCount, 
                             Long tenantsCount, BigDecimal gasSavings, BigDecimal electricitySavings,
                             BigDecimal materialCost) {
        this.regionId = regionId;
        this.regionName = regionName;
        this.propertiesCount = propertiesCount;
        this.tenantsCount = tenantsCount;
        this.totalGasSavingsM3 = gasSavings != null ? gasSavings : BigDecimal.ZERO;
        this.totalElectricitySavingsKwh = electricitySavings != null ? electricitySavings : BigDecimal.ZERO;
        this.totalMaterialCost = materialCost != null ? materialCost : BigDecimal.ZERO;
    }
}
