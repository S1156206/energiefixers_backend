package com.energiefixers.backend.energy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionResultResponse {

    /** Registration invitation token, null if the property already has a tenant account. */
    private String invitationToken;

    public static SubmissionResultResponse withInvitation(String token) {
        SubmissionResultResponse r = new SubmissionResultResponse();
        r.setInvitationToken(token);
        return r;
    }

    public static SubmissionResultResponse noInvitation() {
        return new SubmissionResultResponse();
    }
}
