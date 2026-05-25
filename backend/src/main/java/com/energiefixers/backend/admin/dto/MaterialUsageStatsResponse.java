package com.energiefixers.backend.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class MaterialUsageStatsResponse {
    private Long materialId;
    private String materialName;
    private String category;
    private long totalQuantityUsed;
    private BigDecimal totalCost;
}