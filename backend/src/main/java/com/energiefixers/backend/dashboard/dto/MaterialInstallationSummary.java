package com.energiefixers.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MaterialInstallationSummary {
    private String materialName;
    private String category;
    private long totalQuantity;
}
