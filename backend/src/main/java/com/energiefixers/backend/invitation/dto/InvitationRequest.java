package com.energiefixers.backend.invitation.dto;

import com.energiefixers.backend.invitation.models.Invitation.InvitationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationRequest {
    private Long propertyId;
    private InvitationType type = InvitationType.REGISTRATION;
    private String recipientEmail;
}
