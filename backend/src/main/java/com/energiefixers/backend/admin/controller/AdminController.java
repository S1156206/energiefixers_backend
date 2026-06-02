package com.energiefixers.backend.admin.controller;

import com.energiefixers.backend.admin.dto.*;
import com.energiefixers.backend.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/savings/summary")
    public SavingsSummaryResponse getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return adminService.getSummary(from, to);
    }

    @GetMapping("/savings/by-region")
    public List<RegionStatsResponse> getSavingsByRegion(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return adminService.getSavingsByRegion(regionId, from, to);
    }

    @GetMapping("/savings/by-property")
    public List<PropertySavingsEntry> getSavingsByProperty(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return adminService.getSavingsByProperty(regionId, from, to);
    }

    @GetMapping("/materials")
    public MaterialSummaryResponse getMaterials(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return adminService.getMaterials(regionId, propertyId, from, to);
    }
}
