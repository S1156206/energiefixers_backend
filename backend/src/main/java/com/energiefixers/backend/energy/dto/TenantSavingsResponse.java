package com.energiefixers.backend.energy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TenantSavingsResponse {

    private LocalDate firstVisitDate;
    private boolean hasMeasuredData;
    private BigDecimal estimatedAnnualGasSavingsM3;
    private BigDecimal estimatedAnnualElectricitySavingsKwh;
    private BigDecimal estimatedTotalGasSavedToDateM3;
    private BigDecimal estimatedTotalElectricitySavedToDateKwh;
    private BigDecimal annualGasSavingsM3;
    private BigDecimal annualElectricitySavingsKwh;
    private BigDecimal annualCostSavingsEuros;
    private BigDecimal totalGasSavedToDateM3;
    private BigDecimal totalElectricitySavedToDateKwh;
    private BigDecimal totalCostSavedToDateEuros;
    private int readingsBeforeCount;
    private int readingsAfterCount;
}
