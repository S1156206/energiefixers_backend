package com.energiefixers.backend.social.controller;

import com.energiefixers.backend.social.dto.NeighborhoodSavingsResponse;
import com.energiefixers.backend.social.service.NeighborhoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/neighborhood")
@PreAuthorize("hasRole('TENANT')")
@RequiredArgsConstructor
public class NeighborhoodController {

    private final NeighborhoodService neighborhoodService;

    @GetMapping("/savings")
    public NeighborhoodSavingsResponse getSavings(Authentication authentication) {
        Long userId = ((Number) authentication.getPrincipal()).longValue();
        return neighborhoodService.getNeighborhoodSavings(userId);
    }
}
