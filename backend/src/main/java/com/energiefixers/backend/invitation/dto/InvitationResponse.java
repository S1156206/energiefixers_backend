package com.energiefixers.backend.invitation.dto;

import lombok.Getter;
import lombok.Setter;
import com.energiefixers.backend.invitation.models.Invitation;
import com.energiefixers.backend.invitation.models.Invitation.InvitationType;
import com.energiefixers.backend.invitation.models.Invitation.InvitationStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class InvitationResponse {
    private Long id;
    private Long propertyId;
    private String token;
    private InvitationType type;
    private String recipientEmail;
    private InvitationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;

    public static InvitationResponse from(Invitation invitation) {
        InvitationResponse response = new InvitationResponse();
        response.setId(invitation.getId());
        response.setPropertyId(invitation.getProperty().getId());
        response.setToken(invitation.getToken());
        response.setType(invitation.getType());
        response.setRecipientEmail(invitation.getRecipientEmail());
        response.setStatus(invitation.getStatus());
        response.setSentAt(invitation.getSentAt());
        response.setExpiresAt(invitation.getExpiresAt());
        response.setAcceptedAt(invitation.getAcceptedAt());
        return response;
    }
}
