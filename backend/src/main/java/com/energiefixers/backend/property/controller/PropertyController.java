package com.energiefixers.backend.property.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.energiefixers.backend.energy.dto.CreateSubmissionRequestBody;
import com.energiefixers.backend.energy.service.SubmissionService;
import com.energiefixers.backend.property.dto.MyPropertyResponse;
import com.energiefixers.backend.property.dto.PropertyRequest;
import com.energiefixers.backend.property.dto.PropertyResponse;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.service.PropertyService;
import com.energiefixers.backend.shared.ApiResponse;
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

    /** Tenant: get own property including fix visits */
    @GetMapping("/me")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<MyPropertyResponse>> getMyProperty(Authentication authentication) {
        Long userId = extractUserId(authentication);
        Property property = propertyService.getMyProperty(userId);
        return ResponseEntity.ok(ApiResponse.success(MyPropertyResponse.from(property)));
    }

    /** Staff/admin: get all properties, optionally filtered by region */
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAll(
            @RequestParam(required = false) Long regionId) {
        List<Property> result = regionId != null
            ? propertyService.getAllByRegion(regionId)
            : propertyService.getAll();
        List<PropertyResponse> responses = result.stream()
                .map(PropertyResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /** Staff/admin: get single property */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> getById(@PathVariable Long id) {
        Property property = propertyService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(PropertyResponse.from(property)));
    }

    /** Staff/admin: register a new property after a fix visit */
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> create(@RequestBody PropertyRequest request) {
        Property created = propertyService.create(request);
        return ResponseEntity.status(201).body(ApiResponse.success(PropertyResponse.from(created)));
    }

    /** Staff/admin: add a fix visit to an existing property */
    @PostMapping("/{id}/fix-visits")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<FixVisitResponse>> addFixVisit(
            @PathVariable Long id,
            @RequestBody FixVisitRequest request) {
        FixVisit visit = fixVisitService.addFixVisit(id, request);
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
        return ResponseEntity.ok(ApiResponse.success(PropertyResponse.from(updated)));
    }
}
