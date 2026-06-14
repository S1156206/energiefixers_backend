package com.energiefixers.backend.dashboard.controller;

import com.energiefixers.backend.dashboard.dto.DashboardSavingsResponse;
import com.energiefixers.backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/savings")
    public DashboardSavingsResponse getSavings(
            @RequestParam(required = false) Long regionId) {
        return dashboardService.getSavings(regionId);
    }
}
