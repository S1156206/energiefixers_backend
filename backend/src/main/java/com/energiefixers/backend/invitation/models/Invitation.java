package com.energiefixers.backend.invitation.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.user.models.User;

/**
 * An invitation sent to a tenant to register and link to their property.
 * A new invitation can be created each year to remind tenants to submit
 * their annual energy bill without touching the Property record.
 *
 * Flow:
 *   1. Staff creates a Property + FixVisit
 *   2. Staff triggers an Invitation → token is generated, sent via email/WhatsApp
 *   3. Tenant clicks the link, registers, User is linked to the Property
 *   4. Invitation is marked as accepted
 */
@Entity
@Table(name = "invitation")
@Getter @Setter @NoArgsConstructor
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The property this invitation is for.
     * Multiple invitations per property are allowed (e.g. yearly reminders).
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id")
    private Property property;

    /**
     * Unique token included in the invite link.
     * e.g. https://energiefixers071.nl/register?token=<uuid>
     */
    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationType type;

    @Column(name = "recipient_email")
    public String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    /** When the invitation was sent */
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    /** When the token expires — default 30 days after sending */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** When the tenant accepted the invitation (null if not yet used) */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    /** The user who registered via this invitation */
    @OneToOne
    @JoinColumn(name = "accepted_by_user_id")
    private User acceptedBy;

    @PrePersist
    protected void onCreate() {
        this.token = UUID.randomUUID().toString();
        this.sentAt = LocalDateTime.now();
        this.expiresAt = this.sentAt.plusDays(30);
        this.status = InvitationStatus.PENDING;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isUsable() {
        return this.status == InvitationStatus.PENDING && !isExpired();
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public enum InvitationType {
        /** First-time registration after a fix visit */
        REGISTRATION,
        /** Yearly reminder to submit annual energy bill */
        ANNUAL_REMINDER
    }

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        EXPIRED,
        REVOKED
    }
}