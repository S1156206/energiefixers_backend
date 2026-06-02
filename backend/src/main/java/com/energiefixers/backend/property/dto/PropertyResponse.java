package com.energiefixers.backend.property.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.energiefixers.backend.energy.models.PropertySubmissionRequest;
import com.energiefixers.backend.invitation.models.Invitation;
import com.energiefixers.backend.invitation.models.Invitation.InvitationStatus;
import com.energiefixers.backend.invitation.models.Invitation.InvitationType;
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
    private String tenantEmail;
    private EmailStatus emailStatus;
    private List<InvitationSummary> invitations;
    private List<SubmissionRequestSummary> submissionRequests;

    public enum EmailStatus { NO_EMAIL, OPT_OUT, DELIVERABLE }

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
        response.setTenantEmail(property.getTenantEmail());
        response.setInvitations(
            property.getInvitations() == null ? List.of() :
            property.getInvitations().stream()
                .map(InvitationSummary::from)
                .collect(Collectors.toList())
        );
        response.setSubmissionRequests(
            property.getSubmissionRequests() == null ? List.of() :
            property.getSubmissionRequests().stream()
                .map(SubmissionRequestSummary::from)
                .collect(Collectors.toList())
        );
        return response;
    }

    @Getter
    @Setter
    public static class SubmissionRequestSummary {
        private Long id;
        private String recipientEmail;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime submittedAt;

        public static SubmissionRequestSummary from(PropertySubmissionRequest req) {
            SubmissionRequestSummary summary = new SubmissionRequestSummary();
            summary.setId(req.getId());
            summary.setRecipientEmail(req.getRecipientEmail());
            summary.setCreatedAt(req.getCreatedAt());
            summary.setExpiresAt(req.getExpiresAt());
            summary.setSubmittedAt(req.getSubmittedAt());
            return summary;
        }
    }

    @Getter
    @Setter
    public static class InvitationSummary {
        private Long id;
        private InvitationType type;
        private InvitationStatus status;
        private String recipientEmail;
        private LocalDateTime sentAt;
        private LocalDateTime expiresAt;
        private LocalDateTime acceptedAt;

        public static InvitationSummary from(Invitation invitation) {
            InvitationSummary summary = new InvitationSummary();
            summary.setId(invitation.getId());
            summary.setType(invitation.getType());
            summary.setStatus(invitation.getStatus());
            summary.setRecipientEmail(invitation.getRecipientEmail());
            summary.setSentAt(invitation.getSentAt());
            summary.setExpiresAt(invitation.getExpiresAt());
            summary.setAcceptedAt(invitation.getAcceptedAt());
            return summary;
        }
    }
}
