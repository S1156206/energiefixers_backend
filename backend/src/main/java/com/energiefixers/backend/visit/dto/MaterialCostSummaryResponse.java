package com.energiefixers.backend.visit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MaterialCostSummaryResponse {
    private String materialName;
    private String category;
    private BigDecimal unitPriceEuros;
    private long totalQuantityInstalled;
    private BigDecimal totalCostEuros;
}
