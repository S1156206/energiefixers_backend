package com.energiefixers.backend.property.dto;

import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.models.Property.EnergyLabel;
import com.energiefixers.backend.visit.dto.FixVisitResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class MyPropertyResponse {
    private Long id;
    private String street;
    private String city;
    private String houseNumber;
    private String houseNumberSuffix;
    private String postcode;
    private EnergyLabel energyLabelBefore;
    private EnergyLabel energyLabelAfter;
    private Long regionId;
    private List<FixVisitResponse> fixVisits;

    public static MyPropertyResponse from(Property property) {
        MyPropertyResponse response = new MyPropertyResponse();
        response.setId(property.getId());
        response.setStreet(property.getStreet());
        response.setCity(property.getCity());
        response.setHouseNumber(property.getHouseNumber());
        response.setHouseNumberSuffix(property.getHouseNumberSuffix());
        response.setPostcode(property.getPostcode());
        response.setEnergyLabelBefore(property.getEnergyLabelBefore());
        response.setEnergyLabelAfter(property.getEnergyLabelAfter());
        response.setRegionId(property.getRegion() == null ? null : property.getRegion().getId());
        response.setFixVisits(
            property.getFixVisits().stream()
                .map(FixVisitResponse::from)
                .collect(Collectors.toList())
        );
        return response;
    }
}
