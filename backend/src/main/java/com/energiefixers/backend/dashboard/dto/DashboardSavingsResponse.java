package com.energiefixers.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardSavingsResponse {
    private long totalPropertiesWithData;
    private BigDecimal totalActualCostSavingsEuros;
    private BigDecimal avgMonthlySavingsEuros;
    private List<RegionDashboardEntry> regions;
}
