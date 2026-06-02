package com.energiefixers.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class MaterialSummaryResponse {
    private BigDecimal totalCostEuros;
    private long totalQuantity;
    private List<MaterialUsageStatsResponse> items;
}
