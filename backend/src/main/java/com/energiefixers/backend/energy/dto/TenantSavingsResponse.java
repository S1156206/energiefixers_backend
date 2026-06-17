package com.energiefixers.backend.energy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TenantSavingsResponse {

    private LocalDate firstVisitDate;

    /** True once both before- and after-visit readings are present. */
    private boolean hasMeasuredData;

    // --- Estimates (always present after a fix visit) ---

    /** Annual gas savings based on installed materials (m³/year). */
    private BigDecimal estimatedAnnualGasSavingsM3;

    /** Annual electricity savings based on installed materials (kWh/year). */
    private BigDecimal estimatedAnnualElectricitySavingsKwh;

    /** Running estimate of gas already saved since the last fix visit. */
    private BigDecimal estimatedTotalGasSavedToDateM3;

    /** Running estimate of electricity already saved since the last fix visit. */
    private BigDecimal estimatedTotalElectricitySavedToDateKwh;

    // --- Measured (null when hasMeasuredData = false) ---

    /** Measured annual gas saving: (avg daily before − avg daily after) × 365. */
    private BigDecimal annualGasSavingsM3;

    /** Measured annual electricity saving. */
    private BigDecimal annualElectricitySavingsKwh;

    /** Measured annual cost saving. */
    private BigDecimal annualCostSavingsEuros;

    /** Cumulative gas actually saved across all post-visit readings. */
    private BigDecimal totalGasSavedToDateM3;

    /** Cumulative electricity actually saved across all post-visit readings. */
    private BigDecimal totalElectricitySavedToDateKwh;

    /** Cumulative cost actually saved across all post-visit readings. */
    private BigDecimal totalCostSavedToDateEuros;

    private int readingsBeforeCount;
    private int readingsAfterCount;
}
