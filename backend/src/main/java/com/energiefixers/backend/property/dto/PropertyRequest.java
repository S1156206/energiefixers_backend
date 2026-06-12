package com.energiefixers.backend.property.dto;

import com.energiefixers.backend.property.models.Property.EnergyLabel;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PropertyRequest {
    private String street;
    private String houseNumber;
    private String houseNumberSuffix;
    private String postcode;
    // private EnergyLabel energyLabelBefore;
    // private EnergyLabel energyLabelAfter;
    private String tenantEmail;
    private Long fixRoundId;
}
