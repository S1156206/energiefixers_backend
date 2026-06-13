package com.energiefixers.backend.reports.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardMaterialRowResponse {
    private Long propertyId;
    private String address;
    private Long regionId;
    private String regionName;
    private Long fixRoundId;
    private String fixRoundName;
    private String visitDate;
    private String materialName;
    private Integer quantity;
    private Double cost;
}