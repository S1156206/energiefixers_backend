package com.energiefixers.backend.energy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TenantSavingsResponse {

    private LocalDate firstVisitDate;

    private BigDecimal gasSavingsM3;
    private BigDecimal electricitySavingsKwh;
    private BigDecimal costSavingsEuros;

    private LocalDate baselinePeriodStart;
    private LocalDate baselinePeriodEnd;
    private LocalDate postVisitPeriodStart;
    private LocalDate postVisitPeriodEnd;

    private boolean hasSavingsData;
}
