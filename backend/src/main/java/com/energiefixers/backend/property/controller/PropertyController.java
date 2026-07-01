package com.energiefixers.backend.property.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.energiefixers.backend.energy.dto.CreateSubmissionRequestBody;
import com.energiefixers.backend.energy.service.SubmissionService;
import com.energiefixers.backend.invitation.dto.InvitationRequest;
import com.energiefixers.backend.invitation.models.Invitation;
import com.energiefixers.backend.invitation.service.InvitationService;
import com.energiefixers.backend.property.dto.MyPropertyResponse;
import com.energiefixers.backend.property.dto.PropertyRequest;
import com.energiefixers.backend.property.dto.PropertyResponse;
import com.energiefixers.backend.property.dto.PropertySummaryResponse;
import com.energiefixers.backend.property.dto.PropertyResponse.EmailStatus;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.service.PropertyService;
import com.energiefixers.backend.shared.ApiResponse;
import com.energiefixers.backend.shared.CooldownException;
import com.energiefixers.backend.shared.EmailOptOutService;
import com.energiefixers.backend.visit.dto.FixVisitRequest;
import com.energiefixers.backend.visit.dto.FixVisitResponse;
import com.energiefixers.backend.visit.models.FixVisit;
import com.energiefixers.backend.visit.service.FixVisitService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final FixVisitService fixVisitService;
    private final SubmissionService submissionService;
    private final EmailOptOutService emailOptOutService;
    private final InvitationService invitationService;

    /** Tenant: get own property including fix visits */
    @GetMapping("/me")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<MyPropertyResponse>> getMyProperty(Authentication authentication) {
        Long userId = extractUserId(authentication);
        Property property = propertyService.getMyProperty(userId);
        return ResponseEntity.ok(ApiResponse.success(MyPropertyResponse.from(property)));
    }

    /** Staff/admin: get all properties, optionally filtered by region or fix round */
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PropertySummaryResponse>>> getAll(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long fixRoundId) {
        List<Property> result = fixRoundId != null
            ? propertyService.getAllByFixRound(fixRoundId)
            : regionId != null
                ? propertyService.getAllByRegion(regionId)
                : propertyService.getAll();
        List<PropertySummaryResponse> responses = result.stream()
                .map(p -> enrichedSummary(PropertySummaryResponse.from(p), p.getTenantEmail()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /** Staff/admin: get single property */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> getById(@PathVariable Long id) {
        Property property = propertyService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(enriched(PropertyResponse.from(property), property.getTenantEmail())));
    }

    /** Staff/admin: register a new property after a fix visit */
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> create(@RequestBody PropertyRequest request) {
        Property created = propertyService.create(request);
        PropertyResponse response = enriched(PropertyResponse.from(created), created.getTenantEmail());

        String tenantEmail = request.getTenantEmail();
        if (tenantEmail != null && !tenantEmail.isBlank()) {
            try {
                InvitationRequest invReq = new InvitationRequest();
                invReq.setPropertyId(created.getId());
                invReq.setRecipientEmail(tenantEmail);
                Invitation invitation = invitationService.createInvitation(invReq);
                response.setInvitations(List.of(PropertyResponse.InvitationSummary.from(invitation)));
            } catch (CooldownException e) {
                // Property already has a recent invitation, leave existing list in response
            }
        }

        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    /** Staff/admin: add a fix visit to an existing property */
    @PostMapping("/{id}/fix-visits")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<FixVisitResponse>> addFixVisit(
            @PathVariable Long id,
            @RequestBody FixVisitRequest request) {
        FixVisit visit = fixVisitService.addFixVisit(id, request);

        String tenantEmail = visit.getProperty().getTenantEmail();
        if (tenantEmail != null && !tenantEmail.isBlank()) {
            try {
                submissionService.createSubmissionRequest(id, tenantEmail);
            } catch (CooldownException e) {
                // Recent submission already exists for this property
            }
        }

        return ResponseEntity.status(201).body(ApiResponse.success(FixVisitResponse.from(visit)));
    }

    private Long extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Number) {
            return ((Number) principal).longValue();
        }
        if (principal instanceof String) {
            return Long.parseLong((String) principal);
        }
        throw new IllegalStateException("Unable to determine current user id from authentication principal.");
    }

    /** Staff/admin: send a magic-link submission request to the resident */
    @PostMapping("/{id}/submission-requests")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createSubmissionRequest(
            @PathVariable Long id,
            @RequestBody CreateSubmissionRequestBody body) {
        submissionService.createSubmissionRequest(id, body.getEmail());
        return ResponseEntity.status(201).body(ApiResponse.success(null));
    }

    /** Staff/admin: update property details or energy label */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> update(
            @PathVariable Long id,
            @RequestBody PropertyRequest request) {
        Property updated = propertyService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(enriched(PropertyResponse.from(updated), updated.getTenantEmail())));
    }

    /** Staff/admin: update a fix visit and its installed materials */
    @PutMapping("/{id}/fix-visits/{visitId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<FixVisitResponse>> updateFixVisit(
            @PathVariable Long id,
            @PathVariable Long visitId,
            @RequestBody FixVisitRequest request) {
        FixVisit visit = fixVisitService.updateFixVisit(id, visitId, request);
        return ResponseEntity.ok(ApiResponse.success(FixVisitResponse.from(visit)));
    }

    /** Staff/admin: delete a fix visit and all its installed materials */
    @DeleteMapping("/{id}/fix-visits/{visitId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFixVisit(
            @PathVariable Long id,
            @PathVariable Long visitId) {
        fixVisitService.deleteFixVisit(id, visitId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** Staff/admin: delete a property and all associated data */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        propertyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private PropertyResponse enriched(PropertyResponse response, String email) {
        response.setEmailStatus(resolveEmailStatus(email));
        return response;
    }

    private PropertySummaryResponse enrichedSummary(PropertySummaryResponse response, String email) {
        response.setEmailStatus(resolveEmailStatus(email));
        return response;
    }

    private EmailStatus resolveEmailStatus(String email) {
        if (email == null || email.isBlank()) return EmailStatus.NO_EMAIL;
        if (emailOptOutService.isOptedOut(email)) return EmailStatus.OPT_OUT;
        return EmailStatus.DELIVERABLE;
    }
}
