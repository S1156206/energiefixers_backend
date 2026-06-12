package com.energiefixers.backend.property.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

import com.energiefixers.backend.property.dto.PropertyResponse.EmailStatus;
import com.energiefixers.backend.property.models.Property;

@Getter
@Setter
public class PropertySummaryResponse {

    private Long id;
    private String street;
    private String houseNumber;
    private String houseNumberSuffix;
    private String postcode;
    private Long regionId;
    private String tenantEmail;
    private EmailStatus emailStatus;
    private String invitationStatus;
    private boolean hasEnergyData;
    private Long fixRoundId;
    private String fixRoundName;

    public static PropertySummaryResponse from(Property property) {
        PropertySummaryResponse response = new PropertySummaryResponse();
        response.setId(property.getId());
        response.setStreet(property.getStreet());
        response.setHouseNumber(property.getHouseNumber());
        response.setHouseNumberSuffix(property.getHouseNumberSuffix());
        response.setPostcode(property.getPostcode());
        response.setRegionId(property.getRegion().getId());
        response.setTenantEmail(property.getTenantEmail());

        if (property.getInvitations() != null && !property.getInvitations().isEmpty()) {
            property.getInvitations().stream()
                .max(Comparator.comparing(i -> i.getSentAt()))
                .ifPresent(latest -> response.setInvitationStatus(latest.getStatus().name()));
        } else {
            response.setInvitationStatus("NOT_INVITED");
        }

        response.setHasEnergyData(property.getEnergyReadings() != null && !property.getEnergyReadings().isEmpty());

        if (property.getFixRound() != null) {
            response.setFixRoundId(property.getFixRound().getId());
            response.setFixRoundName(property.getFixRound().getName());
        }
        return response;
    }
}
