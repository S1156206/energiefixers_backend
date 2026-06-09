package com.energiefixers.backend.shared;

import com.energiefixers.backend.invitation.models.Invitation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final EmailOptOutService emailOptOutService;
    private final RestTemplate restTemplate = new RestTemplate(); 

    @Value("${app.mail.from:no-reply@energiefixers071.nl}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.brevo.api-key}")
    private String brevoApiKey;

    @Async
    public void sendInvitation(Invitation invitation) {
        if (invitation.getRecipientEmail() == null || invitation.getRecipientEmail().isBlank()) {
            return;
        }
        if (emailOptOutService.isOptedOut(invitation.getRecipientEmail())) {
            return;
        }

        String unsubscribeToken = emailOptOutService.getOrCreateToken(invitation.getRecipientEmail());
        String invitationLink = String.format("%s/register?token=%s", frontendUrl, invitation.getToken());
        String unsubscribeLink = String.format("%s/unsubscribe/%s", frontendUrl, unsubscribeToken);
        
        String subject = "Energiefixers071 uitnodiging om te registreren";
        String body = "Je bent uitgenodigd om je energierekening bij Energiefixers071 te registreren.\n\n"
                + "Klik op de onderstaande link om je registratie te voltooien:\n"
                + invitationLink
                + "\n\nAls je deze uitnodiging niet verwachtte, negeer dan deze e-mail.\n\n"
                + "---\nGeen e-mails meer ontvangen? " + unsubscribeLink;

        sendEmailViaBrevo(invitation.getRecipientEmail(), subject, body);
    }

    @Async
    public void sendSubmissionRequest(String recipientEmail, String token, String propertyAddress) {
        if (emailOptOutService.isOptedOut(recipientEmail)) {
            return;
        }

        String unsubscribeToken = emailOptOutService.getOrCreateToken(recipientEmail);
        String submissionLink = String.format("%s/submit/%s", frontendUrl, token);
        String unsubscribeLink = String.format("%s/unsubscribe/%s", frontendUrl, unsubscribeToken);
        
        String subject = "Energiefixers071 - Geef uw jaarverbruik door";
        String body = "Energiefixers071 heeft bij uw woning (" + propertyAddress + ") energiebesparende materialen geïnstalleerd.\n\n"
                + "Wij vragen u uw jaarlijkse energieverbruik door te geven, zodat we kunnen zien hoeveel u heeft bespaard.\n\n"
                + "Klik op de link hieronder om uw verbruik in te vullen:\n"
                + submissionLink
                + "\n\nDeze link is 90 dagen geldig. U hoeft geen account aan te maken.\n\n"
                + "Als u deze e-mail niet verwachtte, kunt u deze negeren.\n\n"
                + "---\nGeen e-mails meer ontvangen? " + unsubscribeLink;

        sendEmailViaBrevo(recipientEmail, subject, body);
    }

    private void sendEmailViaBrevo(String toEmail, String subject, String textContent) {
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        // Bouw de JSON payload op zoals Brevo die verwacht
        Map<String, Object> requestBody = Map.of(
                "sender", Map.of("name", "Energiefixers071", "email", fromAddress),
                "to", List.of(Map.of("email", toEmail)),
                "subject", subject,
                "textContent", textContent
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            log.info("Email successfully sent to {} via Brevo REST API", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", toEmail, ex.getMessage());
        }
    }
}