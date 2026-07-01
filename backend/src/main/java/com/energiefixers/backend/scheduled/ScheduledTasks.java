package com.energiefixers.backend.scheduled;

import com.energiefixers.backend.energy.repository.PropertySubmissionRequestRepository;
import com.energiefixers.backend.energy.service.SubmissionService;
import com.energiefixers.backend.invitation.service.InvitationService;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.repository.PropertyRepository;
import com.energiefixers.backend.shared.CooldownException;
import com.energiefixers.backend.visit.repository.FixVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final InvitationService invitationService;
    private final SubmissionService submissionService;
    private final PropertyRepository propertyRepository;
    private final FixVisitRepository fixVisitRepository;
    private final PropertySubmissionRequestRepository submissionRequestRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void expireStaleInvitations() {
        log.info("Running scheduled task: expireStaleInvitations");
        invitationService.expireStale();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void sendAnnualSubmissions() {
        log.info("Running scheduled task: sendAnnualSubmissions");
        LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(365);

        List<Property> properties = propertyRepository.findAll();
        for (Property property : properties) {
            String email = property.getTenantEmail();
            if (email == null || email.isBlank()) continue;

            Long propId = property.getId();

            // Only send to properties that have had at least one fix visit
            if (fixVisitRepository.findAllByPropertyId(propId).isEmpty()) continue;

            // Send if no previous submission exists, or the last one was over a year ago
            boolean shouldSend = submissionRequestRepository
                    .findTopByPropertyIdOrderByCreatedAtDesc(propId)
                    .map(last -> last.getCreatedAt().isBefore(oneYearAgo))
                    .orElse(true);

            if (!shouldSend) continue;

            try {
                submissionService.createSubmissionRequest(propId, email);
                log.info("Annual submission sent to {} for property {}", email, propId);
            } catch (CooldownException e) {
                log.warn("Cooldown active for property {}, skipping annual submission", propId);
            } catch (Exception e) {
                log.error("Failed to send annual submission for property {}: {}", propId, e.getMessage());
            }
        }
    }
}
