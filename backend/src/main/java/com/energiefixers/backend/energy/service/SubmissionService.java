package com.energiefixers.backend.energy.service;

import com.energiefixers.backend.energy.dto.SubmissionFormRequest;
import com.energiefixers.backend.energy.dto.SubmissionInfoResponse;
import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.models.PropertySubmissionRequest;
import com.energiefixers.backend.energy.repository.EnergyReadingRepository;
import com.energiefixers.backend.energy.repository.PropertySubmissionRequestRepository;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.repository.PropertyRepository;
import com.energiefixers.backend.shared.MailService;
import com.energiefixers.backend.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final PropertySubmissionRequestRepository submissionRequestRepository;
    private final PropertyRepository propertyRepository;
    private final EnergyReadingRepository energyReadingRepository;
    private final MailService mailService;

    @Transactional
    public void createSubmissionRequest(Long propertyId, String email) {
        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new NotFoundException("Property not found: " + propertyId));

        PropertySubmissionRequest request = new PropertySubmissionRequest();
        request.setProperty(property);
        request.setToken(UUID.randomUUID().toString());
        request.setRecipientEmail(email);
        request.setExpiresAt(LocalDateTime.now().plusDays(90));

        submissionRequestRepository.save(request);
        mailService.sendSubmissionRequest(email, request.getToken(), buildAddress(property));
    }

    public SubmissionInfoResponse getSubmissionInfo(String token) {
        PropertySubmissionRequest request = resolveActiveRequest(token);
        return SubmissionInfoResponse.from(request.getProperty());
    }

    @Transactional
    public void submitReading(String token, SubmissionFormRequest body) {
        PropertySubmissionRequest request = resolveActiveRequest(token);

        EnergyReading reading = new EnergyReading();
        reading.setProperty(request.getProperty());
        reading.setGasUsageM3(body.getGasUsageM3());
        reading.setElectricityUsageKwh(body.getElectricityUsageKwh());
        reading.setTotalCostEuros(body.getTotalCostEuros());
        reading.setPeriodStart(body.getPeriodStart());
        reading.setPeriodEnd(body.getPeriodEnd());
        reading.setSourceType(EnergyReading.SourceType.ANNUAL_BILL_MANUAL);

        energyReadingRepository.save(reading);

        request.setSubmittedAt(LocalDateTime.now());
        submissionRequestRepository.save(request);
    }

    private PropertySubmissionRequest resolveActiveRequest(String token) {
        PropertySubmissionRequest request = submissionRequestRepository.findByToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ongeldige link"));

        if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Deze link is verlopen");
        }

        if (request.getSubmittedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Dit formulier is al ingevuld");
        }

        return request;
    }

    private String buildAddress(Property p) {
        return p.getStreet() + " " + p.getHouseNumber()
            + (p.getHouseNumberSuffix() != null ? p.getHouseNumberSuffix() : "")
            + ", " + p.getPostcode();
    }
}
