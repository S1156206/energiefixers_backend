package com.energiefixers.backend.property.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.energiefixers.backend.property.dto.RegionResponse;
import com.energiefixers.backend.property.repository.RegionRepository;
import com.energiefixers.backend.shared.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionRepository regionRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RegionResponse>>> getAll() {
        List<RegionResponse> regions = regionRepository.findAll().stream()
                .map(RegionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(regions));
    }
}
