package com.energiefixers.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RegionCombinedEntry {
    private Long regionId;
    private String regionName;
    private long propertiesWithActualData;
    private BigDecimal totalActualSavingsEuros;
    private long propertiesWithEstimatedOnly;
    private BigDecimal totalEstimatedSavingsEuros;
    private BigDecimal totalCombinedSavingsEuros;
}
