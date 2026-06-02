package com.energiefixers.backend.shared;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOptOutRepository extends JpaRepository<EmailOptOut, Long> {
    Optional<EmailOptOut> findByEmail(String email);
    Optional<EmailOptOut> findByUnsubscribeToken(String token);
}
