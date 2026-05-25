package com.energiefixers.backend.visit.dto;

import lombok.Getter;
import lombok.Setter;
import com.energiefixers.backend.visit.models.InstalledMaterial;

@Getter
@Setter
public class InstalledMaterialResponse {
    private Long materialId;
    private String materialName;
    private int quantity;

    public static InstalledMaterialResponse from(InstalledMaterial installedMaterial) {
        InstalledMaterialResponse response = new InstalledMaterialResponse();
        response.setMaterialId(installedMaterial.getMaterial().getId());
        response.setMaterialName(installedMaterial.getMaterial().getName());
        response.setQuantity(installedMaterial.getQuantity());
        return response;
    }
}
