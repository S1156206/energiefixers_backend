package com.energiefixers.backend.shared;

import com.energiefixers.backend.invitation.models.Invitation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@energiefixers071.nl}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendInvitation(Invitation invitation) {
        if (invitation.getRecipientEmail() == null || invitation.getRecipientEmail().isBlank()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(invitation.getRecipientEmail());
            helper.setSubject("Energiefixers071 uitnodiging om te registreren");

            String invitationLink = String.format("%s/register?token=%s", frontendUrl, invitation.getToken());
            String body = "Je bent uitgenodigd om je energierekening bij Energiefixers071 te registreren.\n\n"
                    + "Klik op de onderstaande link om je registratie te voltooien:\n"
                    + invitationLink
                    + "\n\nAls je deze uitnodiging niet verwachtte, negeer dan deze e-mail.";

            helper.setText(body);
            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send invitation email", ex);
        }
    }

    public void sendSubmissionRequest(String recipientEmail, String token, String propertyAddress) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);
            helper.setSubject("Energiefixers071 - Geef uw jaarverbruik door");

            String submissionLink = String.format("%s/submit/%s", frontendUrl, token);
            String body = "Energiefixers071 heeft bij uw woning (" + propertyAddress + ") energiebesparende materialen geïnstalleerd.\n\n"
                    + "Wij vragen u uw jaarlijkse energieverbruik door te geven, zodat we kunnen zien hoeveel u heeft bespaard.\n\n"
                    + "Klik op de link hieronder om uw verbruik in te vullen:\n"
                    + submissionLink
                    + "\n\nDeze link is 90 dagen geldig. U hoeft geen account aan te maken.\n\n"
                    + "Als u deze e-mail niet verwachtte, kunt u deze negeren.";

            helper.setText(body);
            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send submission request email", ex);
        }
    }
}
