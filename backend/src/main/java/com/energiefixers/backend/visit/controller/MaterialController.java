package com.energiefixers.backend.visit.controller;

import com.energiefixers.backend.shared.ApiResponse;
import com.energiefixers.backend.visit.dto.MaterialCostSummaryResponse;
import com.energiefixers.backend.visit.dto.MaterialRequest;
import com.energiefixers.backend.visit.dto.MaterialResponse;
import com.energiefixers.backend.visit.models.Material;
import com.energiefixers.backend.visit.repository.FixVisitRepository;
import com.energiefixers.backend.visit.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialRepository materialRepository;
    private final FixVisitRepository fixVisitRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MaterialResponse>>> getAll() {
        List<MaterialResponse> materials = materialRepository.findAll().stream()
                .map(MaterialResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(materials));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MaterialResponse>> create(@RequestBody MaterialRequest request) {
        Material material = new Material();
        material.setName(request.getName());
        material.setDescription(request.getDescription());
        material.setPriceEuros(request.getPriceEuros());
        material.setEstimatedGasSavingM3(request.getEstimatedGasSavingM3());
        material.setEstimatedElectricitySavingKwh(request.getEstimatedElectricitySavingKwh());
        material.setCategory(request.getCategory());
        material.setUnit(request.getUnit());
        Material saved = materialRepository.save(material);
        return ResponseEntity.status(201).body(ApiResponse.success(MaterialResponse.from(saved)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MaterialResponse>> update(@PathVariable Long id, @RequestBody MaterialRequest request) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material niet gevonden"));
        material.setName(request.getName());
        material.setDescription(request.getDescription());
        material.setPriceEuros(request.getPriceEuros());
        material.setEstimatedGasSavingM3(request.getEstimatedGasSavingM3());
        material.setEstimatedElectricitySavingKwh(request.getEstimatedElectricitySavingKwh());
        material.setCategory(request.getCategory());
        material.setUnit(request.getUnit());
        Material saved = materialRepository.save(material);
        return ResponseEntity.ok(ApiResponse.success(MaterialResponse.from(saved)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material niet gevonden"));
        if (materialRepository.isUsedInAnyVisit(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Materiaal is gekoppeld aan bestaande bezoeken en kan niet verwijderd worden");
        }
        materialRepository.delete(material);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/cost-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MaterialCostSummaryResponse>>> getCostSummary() {
        List<MaterialCostSummaryResponse> summary = fixVisitRepository.getMaterialCostSummary().stream()
                .map(row -> {
                    String name = (String) row[0];
                    String category = row[1].toString();
                    BigDecimal unitPrice = (BigDecimal) row[2];
                    long qty = ((Number) row[3]).longValue();
                    BigDecimal totalCost = unitPrice != null
                            ? unitPrice.multiply(BigDecimal.valueOf(qty))
                            : BigDecimal.ZERO;
                    return new MaterialCostSummaryResponse(name, category, unitPrice, qty, totalCost);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
