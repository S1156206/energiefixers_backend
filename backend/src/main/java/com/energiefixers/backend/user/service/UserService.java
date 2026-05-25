package com.energiefixers.backend.user.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energiefixers.backend.invitation.dto.RegistrationRequest;
import com.energiefixers.backend.invitation.models.Invitation;
import com.energiefixers.backend.invitation.repository.InvitationRepository;
import com.energiefixers.backend.user.models.Role;
import com.energiefixers.backend.user.models.User;
import com.energiefixers.backend.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerFromInvitation(Invitation invitation, RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Er bestaat al een account met dit e-mailadres.");
        }

        if (invitation.getProperty().getTenant() != null) {
            throw new IllegalStateException("Deze woning heeft al een gekoppelde huurder.");
        }

        if (invitation.getRecipientEmail() != null && !invitation.getRecipientEmail().equalsIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Het e-mailadres komt niet overeen met de uitnodiging.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.TENANT);
        user.setProperty(invitation.getProperty());

        User savedUser = userRepository.save(user);

        invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitation.setAcceptedBy(savedUser);
        invitationRepository.save(invitation);
    }
}
