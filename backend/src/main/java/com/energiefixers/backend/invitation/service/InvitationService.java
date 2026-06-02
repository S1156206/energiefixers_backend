package com.energiefixers.backend.invitation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energiefixers.backend.invitation.dto.InvitationRequest;
import com.energiefixers.backend.invitation.models.Invitation;
import com.energiefixers.backend.invitation.models.Invitation.InvitationStatus;
import com.energiefixers.backend.invitation.models.Invitation.InvitationType;
import com.energiefixers.backend.invitation.repository.InvitationRepository;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.repository.PropertyRepository;
import com.energiefixers.backend.shared.MailService;
import com.energiefixers.backend.shared.NotFoundException;
import com.energiefixers.backend.user.models.User;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final PropertyRepository propertyRepository;
    private final MailService mailService;

    /**
     * Creates and sends an invitation for a property.
     * Used both for first-time registration and yearly reminders.
     */
    @Transactional
    public Invitation createInvitation(InvitationRequest request) {
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new NotFoundException("Property not found: " + request.getPropertyId()));

        // Revoke any open pending invitations of the same type first
        invitationRepository
                .findAllByPropertyIdAndTypeAndStatus(request.getPropertyId(), request.getType(), InvitationStatus.PENDING)
                .forEach(existing -> {
                    existing.setStatus(InvitationStatus.REVOKED);
                    invitationRepository.save(existing);
                });

        Invitation invitation = new Invitation();
        invitation.setProperty(property);
        invitation.setType(request.getType());
        invitation.setRecipientEmail(request.getRecipientEmail());
        // token, sentAt, expiresAt and status are set via @PrePersist

        Invitation saved = invitationRepository.save(invitation);
        mailService.sendInvitation(saved);
        return saved;
    }

    @Transactional
    public Invitation createInvitation(Long propertyId, InvitationType type) {
        InvitationRequest request = new InvitationRequest();
        request.setPropertyId(propertyId);
        request.setType(type);
        return createInvitation(request);
    }

    /**
     * Validates the token and returns the invitation if usable.
     * Called when a tenant clicks the invite link.
     */
    public Invitation validateToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
            .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (!invitation.isUsable()) {
            throw new IllegalStateException("Invitation is no longer valid");
        }

        return invitation;
    }

    /**
     * Marks the invitation as accepted and links it to the newly registered user.
     * Called immediately after the User is created during registration.
     */
    @Transactional
    public void accept(Invitation invitation, User user) {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitation.setAcceptedBy(user);
        invitationRepository.save(invitation);
    }

    /**
     * Creates a REGISTRATION invitation without sending an email.
     * Used after an anonymous submission to offer inline account creation.
     * Returns the raw token so the caller can embed it in a response.
     */
    @Transactional
    public String createRegistrationInvitation(Long propertyId, String recipientEmail) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property not found: " + propertyId));

        invitationRepository
                .findAllByPropertyIdAndTypeAndStatus(propertyId, InvitationType.REGISTRATION, InvitationStatus.PENDING)
                .forEach(existing -> {
                    existing.setStatus(InvitationStatus.REVOKED);
                    invitationRepository.save(existing);
                });

        Invitation invitation = new Invitation();
        invitation.setProperty(property);
        invitation.setType(InvitationType.REGISTRATION);
        invitation.setRecipientEmail(recipientEmail);

        return invitationRepository.save(invitation).getToken();
    }

    /**
     * Expires all pending invitations whose expiresAt has passed.
     * Hook this up to a @Scheduled job, e.g. nightly.
     */
    @Transactional
    public void expireStale() {
        invitationRepository
            .findAllByStatusAndExpiresAtBefore(InvitationStatus.PENDING, LocalDateTime.now())
            .forEach(inv -> {
                inv.setStatus(InvitationStatus.EXPIRED);
                invitationRepository.save(inv);
            });
    }
}
