package com.energiefixers.backend.property.dto;

import lombok.Getter;
import lombok.Setter;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.models.Property.EnergyLabel;

@Getter
@Setter
public class PropertyResponse {
    private Long id;
    private String street;
    private String houseNumber;
    private String houseNumberSuffix;
    private String postcode;
    private EnergyLabel energyLabelBefore;
    private EnergyLabel energyLabelAfter;
    private Long regionId;

    public static PropertyResponse from(Property property) {
        PropertyResponse response = new PropertyResponse();
        response.setId(property.getId());
        response.setStreet(property.getStreet());
        response.setHouseNumber(property.getHouseNumber());
        response.setHouseNumberSuffix(property.getHouseNumberSuffix());
        response.setPostcode(property.getPostcode());
        response.setEnergyLabelBefore(property.getEnergyLabelBefore());
        response.setEnergyLabelAfter(property.getEnergyLabelAfter());
        response.setRegionId(property.getRegion().getId());
        return response;
    }
}
