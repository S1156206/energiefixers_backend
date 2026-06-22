package com.energiefixers.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class FixRoundChartEntry {
    private Long fixRoundId;
    private String fixRoundName;
    private BigDecimal co2SavedKg;
    private BigDecimal savingsEuros;
    private long propertiesHelped;
    private BigDecimal gasSavedM3;
    private BigDecimal electricitySavedKwh;
}