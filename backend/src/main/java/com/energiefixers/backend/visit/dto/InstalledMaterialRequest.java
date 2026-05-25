package com.energiefixers.backend.visit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstalledMaterialRequest {
    private Long materialId;
    private int quantity;
}
