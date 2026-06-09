package com.energiefixers.backend.energy.dto;

import lombok.Getter;
import lombok.Setter;
import com.energiefixers.backend.energy.models.PropertySubmissionRequest;
import java.time.LocalDateTime;

@Getter @Setter
public class SubmissionResponse {
    private Long id;
    private Long propertyId;
    private String token;
    private String recipientEmail;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime nextMailAvailableAt;

    public static SubmissionResponse from(PropertySubmissionRequest r) {
        SubmissionResponse dto = new SubmissionResponse();
        dto.setId(r.getId());
        dto.setPropertyId(r.getProperty().getId());
        dto.setToken(r.getToken());
        dto.setRecipientEmail(r.getRecipientEmail());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setExpiresAt(r.getExpiresAt());
        return dto;
    }
}
