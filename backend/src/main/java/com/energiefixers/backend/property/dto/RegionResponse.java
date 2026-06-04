package com.energiefixers.backend.property.dto;

import com.energiefixers.backend.property.models.Region;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class RegionResponse {

    private Long id;
    private String name;
    private String postcodePrefix;

    public static RegionResponse from(Region region) {
        return new RegionResponse(region.getId(), region.getName(), region.getPostcodePrefix());
    }
}
