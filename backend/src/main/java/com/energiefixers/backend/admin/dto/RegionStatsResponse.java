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
    private Long visitsCount;
    private BigDecimal totalGasSavingsM3;
    private BigDecimal totalElectricitySavingsKwh;
    private BigDecimal totalMaterialCost;
    private BigDecimal actualGasSavingsM3;
    private BigDecimal actualElectricitySavingsKwh;
    private BigDecimal actualCostSavingsEuros;
    private long propertiesWithActualData;

    public RegionStatsResponse(Long regionId, String regionName, Long propertiesCount,
                               Long visitsCount, BigDecimal gasSavings, BigDecimal electricitySavings,
                               BigDecimal materialCost, BigDecimal actualGas, BigDecimal actualElec,
                               BigDecimal actualCost, long propertiesWithActualData) {
        this.regionId = regionId;
        this.regionName = regionName;
        this.propertiesCount = propertiesCount;
        this.visitsCount = visitsCount;
        this.totalGasSavingsM3 = gasSavings != null ? gasSavings : BigDecimal.ZERO;
        this.totalElectricitySavingsKwh = electricitySavings != null ? electricitySavings : BigDecimal.ZERO;
        this.totalMaterialCost = materialCost != null ? materialCost : BigDecimal.ZERO;
        this.actualGasSavingsM3 = actualGas;
        this.actualElectricitySavingsKwh = actualElec;
        this.actualCostSavingsEuros = actualCost;
        this.propertiesWithActualData = propertiesWithActualData;
    }
}
