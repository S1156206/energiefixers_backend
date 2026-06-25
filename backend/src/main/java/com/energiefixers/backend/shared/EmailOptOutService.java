package com.energiefixers.backend.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailOptOutService {

    private final EmailOptOutRepository emailOptOutRepository;

    public boolean isOptedOut(String email) {
        return emailOptOutRepository.findByEmail(normalize(email))
            .map(EmailOptOut::isOptedOut)
            .orElse(false);
    }

    /** Returns the unsubscribe token for this email, creating a record if needed. */
    @Transactional
    public String getOrCreateToken(String email) {
        return emailOptOutRepository.findByEmail(normalize(email))
            .orElseGet(() -> {
                EmailOptOut record = new EmailOptOut();
                record.setEmail(normalize(email));
                return emailOptOutRepository.save(record);
            })
            .getUnsubscribeToken();
    }

    @Transactional
    public void optOut(String token) {
        EmailOptOut record = emailOptOutRepository.findByUnsubscribeToken(token)
            .orElseThrow(() -> new NotFoundException("Unsubscribe token not found"));
        if (!record.isOptedOut()) {
            record.setOptedOutAt(LocalDateTime.now());
            emailOptOutRepository.save(record);
        }
    }

    @Transactional
    public void optOutByEmail(String email) {
        EmailOptOut record = emailOptOutRepository.findByEmail(normalize(email))
            .orElseGet(() -> {
                EmailOptOut newRecord = new EmailOptOut();
                newRecord.setEmail(normalize(email));
                newRecord.setOptedOutAt(LocalDateTime.now());
                return emailOptOutRepository.save(newRecord);
            });
        if (!record.isOptedOut()) {
            record.setOptedOutAt(LocalDateTime.now());
        }
    }

    @Transactional
    public void optInByEmail(String email) {
        emailOptOutRepository.findByEmail(normalize(email))
            .ifPresent(record -> {
                record.setOptedOutAt(null);
                emailOptOutRepository.save(record);
            });
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
