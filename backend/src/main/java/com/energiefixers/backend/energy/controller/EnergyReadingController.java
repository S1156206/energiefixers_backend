package com.energiefixers.backend.energy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.energiefixers.backend.energy.dto.EnergyReadingRequest;
import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.service.EnergyReadingService;
import com.energiefixers.backend.shared.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/energy-readings")
@RequiredArgsConstructor
public class EnergyReadingController {

    private final EnergyReadingService energyReadingService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<List<EnergyReading>>> getMine(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(ApiResponse.success(energyReadingService.findForTenant(userId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<EnergyReading>> create(
            @RequestBody EnergyReadingRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        EnergyReading saved = energyReadingService.createForTenant(userId, request);
        return ResponseEntity.status(201).body(ApiResponse.success(saved));
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
