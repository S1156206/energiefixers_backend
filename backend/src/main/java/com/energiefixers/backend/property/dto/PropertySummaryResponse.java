package com.energiefixers.backend.property.dto;

import lombok.Getter;
import lombok.Setter;

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
    private boolean hasInvitations;
    private boolean hasSubmissionRequests;

    public static PropertySummaryResponse from(Property property) {
        PropertySummaryResponse response = new PropertySummaryResponse();
        response.setId(property.getId());
        response.setStreet(property.getStreet());
        response.setHouseNumber(property.getHouseNumber());
        response.setHouseNumberSuffix(property.getHouseNumberSuffix());
        response.setPostcode(property.getPostcode());
        response.setRegionId(property.getRegion().getId());
        response.setTenantEmail(property.getTenantEmail());
        response.setHasInvitations(property.getInvitations() != null && !property.getInvitations().isEmpty());
        response.setHasSubmissionRequests(property.getSubmissionRequests() != null && !property.getSubmissionRequests().isEmpty());
        return response;
    }
}
