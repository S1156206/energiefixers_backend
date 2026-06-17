package com.energiefixers.backend.property.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import com.energiefixers.backend.energy.models.PropertySubmissionRequest;
import com.energiefixers.backend.invitation.models.Invitation;
import com.energiefixers.backend.invitation.models.Invitation.InvitationStatus;
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
    private String tenantStatus;
    private Long fixRoundId;
    private String fixRoundName;

    public static PropertySummaryResponse from(Property property) {
        PropertySummaryResponse response = new PropertySummaryResponse();
        response.setId(property.getId());
        response.setStreet(property.getStreet());
        response.setHouseNumber(property.getHouseNumber());
        response.setHouseNumberSuffix(property.getHouseNumberSuffix());
        response.setPostcode(property.getPostcode());
        response.setRegionId(property.getRegion() == null ? null : property.getRegion().getId());
        response.setTenantEmail(property.getTenantEmail());
        response.setTenantStatus(resolveTenantStatus(property));
        if (property.getFixRound() != null) {
            response.setFixRoundId(property.getFixRound().getId());
            response.setFixRoundName(property.getFixRound().getName());
        }
        return response;
    }

    private static String resolveTenantStatus(Property property) {
        List<?> readings = property.getEnergyReadings();
        if (readings != null && !readings.isEmpty()) {
            return "DATA_PRESENT";
        }

        List<PropertySubmissionRequest> requests = property.getSubmissionRequests();
        if (requests != null) {
            boolean hasActiveLink = requests.stream().anyMatch(r ->
                r.getSubmittedAt() == null && r.getExpiresAt().isAfter(LocalDateTime.now())
            );
            if (hasActiveLink) {
                return "LINK_SENT";
            }
        }

        List<Invitation> invitations = property.getInvitations();
        if (invitations != null && !invitations.isEmpty()) {
            Invitation latest = invitations.stream()
                .max(Comparator.comparing(Invitation::getSentAt))
                .orElse(null);
            if (latest != null) {
                if (latest.getStatus() == InvitationStatus.ACCEPTED) return "REGISTERED";
                if (latest.getStatus() == InvitationStatus.PENDING)  return "INVITED";
                if (latest.getStatus() == InvitationStatus.EXPIRED)  return "INVITE_EXPIRED";
            }
        }

        return "NOT_INVITED";
    }
}
