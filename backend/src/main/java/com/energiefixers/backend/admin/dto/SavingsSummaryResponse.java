package com.energiefixers.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SavingsSummaryResponse {
    private long totalProperties;
    private long totalVisits;
    private BigDecimal totalMaterialCostEuros;
    private BigDecimal estimatedGasSavingsM3;
    private BigDecimal estimatedElectricitySavingsKwh;
    private BigDecimal actualGasSavingsM3;
    private BigDecimal actualElectricitySavingsKwh;
    private BigDecimal actualCostSavingsEuros;
    private long propertiesWithActualData;
}
