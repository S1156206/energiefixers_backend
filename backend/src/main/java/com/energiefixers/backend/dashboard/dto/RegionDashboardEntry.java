package com.energiefixers.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RegionDashboardEntry {
    private Long regionId;
    private String regionName;
    private long propertiesWithData;
    private BigDecimal totalActualCostSavingsEuros;
    private BigDecimal avgMonthlySavingsEuros;
}
