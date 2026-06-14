package com.energiefixers.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardCombinedSavingsResponse {
    private BigDecimal totalActualSavingsEuros;
    private BigDecimal totalEstimatedSavingsEuros;
    private BigDecimal totalCombinedSavingsEuros;
    private List<RegionCombinedEntry> regions;
}
