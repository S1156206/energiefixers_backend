package com.energiefixers.backend.energy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.energiefixers.backend.property.models.Property;

@Entity
@Table(name = "property_submission_request")
@Getter @Setter @NoArgsConstructor
public class PropertySubmissionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Link is valid for 90 days after creation */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Null until the resident submits the form */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
