package com.energiefixers.backend.invitation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energiefixers.backend.invitation.models.*;
import com.energiefixers.backend.invitation.models.Invitation.InvitationStatus;
import com.energiefixers.backend.invitation.models.Invitation.InvitationType;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findAllByStatusAndExpiresAtBefore(InvitationStatus status, LocalDateTime before);
    Optional<Invitation> findByToken(String token);

    List<Invitation> findAllByPropertyIdAndTypeAndStatus(Long id, InvitationType type, InvitationStatus status);
}
