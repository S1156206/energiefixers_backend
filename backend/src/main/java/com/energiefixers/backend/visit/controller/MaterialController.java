package com.energiefixers.backend.visit.controller;

import com.energiefixers.backend.shared.ApiResponse;
import com.energiefixers.backend.visit.dto.MaterialResponse;
import com.energiefixers.backend.visit.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialRepository materialRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MaterialResponse>>> getAll() {
        List<MaterialResponse> materials = materialRepository.findAll().stream()
                .map(MaterialResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(materials));
    }
}
