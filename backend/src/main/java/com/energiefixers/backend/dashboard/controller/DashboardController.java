package com.energiefixers.backend.dashboard.controller;

import com.energiefixers.backend.dashboard.dto.DashboardCombinedSavingsResponse;
import com.energiefixers.backend.dashboard.dto.DashboardSavingsResponse;
import com.energiefixers.backend.dashboard.dto.MaterialInstallationSummary;
import com.energiefixers.backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/combined-savings")
    public DashboardCombinedSavingsResponse getCombinedSavings(
            @RequestParam(required = false) Long regionId) {
        return dashboardService.getCombinedSavings(regionId);
    }

    @GetMapping("/materials")
    public List<MaterialInstallationSummary> getMaterialsSummary(
            @RequestParam(required = false) Long fixRoundId) {
        return dashboardService.getMaterialsSummary(fixRoundId);
    }
}