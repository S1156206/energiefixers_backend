package com.energiefixers.backend.property.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.energiefixers.backend.property.dto.PropertyRequest;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.service.PropertyService;
import com.energiefixers.backend.shared.ApiResponse;
import com.energiefixers.backend.visit.dto.FixVisitRequest;
import com.energiefixers.backend.visit.models.FixVisit;
import com.energiefixers.backend.visit.service.FixVisitService;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final FixVisitService fixVisitService;

    /** Staff/admin: get all properties, optionally filtered by region */
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Property>>> getAll(
            @RequestParam(required = false) Long regionId) {
        List<Property> result = regionId != null
            ? propertyService.getAllByRegion(regionId)
            : propertyService.getAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** Staff/admin: get single property */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Property>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(propertyService.getById(id)));
    }

    /** Staff/admin: register a new property after a fix visit */
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Property>> create(@RequestBody PropertyRequest request) {
        Property created = propertyService.create(request);
        return ResponseEntity.status(201).body(ApiResponse.success(created));
    }

    /** Staff/admin: add a fix visit to an existing property */
    @PostMapping("/{id}/fix-visits")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<FixVisit>> addFixVisit(
            @PathVariable Long id,
            @RequestBody FixVisitRequest request) {
        FixVisit visit = fixVisitService.addFixVisit(id, request);
        return ResponseEntity.status(201).body(ApiResponse.success(visit));
    }

    /** Staff/admin: update property details or energy label */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Property>> update(
            @PathVariable Long id,
            @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(propertyService.update(id, request)));
    }
}
