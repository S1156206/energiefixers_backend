package com.energiefixers.backend.reports.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.energiefixers.backend.reports.dto.DashboardMaterialRowResponse;
import com.energiefixers.backend.reports.service.ReportService;
import com.energiefixers.backend.shared.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/installed-materials")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DashboardMaterialRowResponse>>> getInstalledMaterials() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getInstalledMaterialsReport()));
    }
}