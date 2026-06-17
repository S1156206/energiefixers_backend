package com.energiefixers.backend.energy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.energiefixers.backend.energy.dto.EnergyReadingRequest;
import com.energiefixers.backend.energy.dto.EnergyReadingResponse;
import com.energiefixers.backend.energy.dto.TenantSavingsResponse;
import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.service.EnergyReadingService;
import com.energiefixers.backend.shared.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/energy-readings")
@RequiredArgsConstructor
public class EnergyReadingController {

    private final EnergyReadingService energyReadingService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<List<EnergyReadingResponse>>> getMine(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<EnergyReading> readings = energyReadingService.findForTenant(userId);
        List<EnergyReadingResponse> responses = readings.stream()
                .map(EnergyReadingResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<EnergyReadingResponse>> create(
            @RequestBody EnergyReadingRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        EnergyReading saved = energyReadingService.createForTenant(userId, request);
        return ResponseEntity.status(201).body(ApiResponse.success(EnergyReadingResponse.from(saved)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<EnergyReadingResponse>> update(
            @PathVariable Long id,
            @RequestBody EnergyReadingRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        EnergyReading updated = energyReadingService.updateForTenant(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(EnergyReadingResponse.from(updated)));
    }

    @GetMapping("/savings")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<TenantSavingsResponse> getSavings(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(energyReadingService.getSavingsForTenant(userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            Authentication authentication) {
        energyReadingService.deleteForTenant(extractUserId(authentication), id);
        return ResponseEntity.ok(ApiResponse.success(null));
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
}
