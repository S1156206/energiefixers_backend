package com.energiefixers.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class PublicOverviewResponse {
    private long totalFixRounds;
    private long totalPropertiesHelped;
    private BigDecimal totalSavingsEuros;
    private BigDecimal totalGasSavedM3;
    private BigDecimal totalElectricitySavedKwh;
    private BigDecimal totalCo2SavedKg;
    private List<FixRoundChartEntry> chartData;
}