package com.energiefixers.backend.property.controller;

import com.energiefixers.backend.property.dto.FixRoundRequest;
import com.energiefixers.backend.property.dto.FixRoundResponse;
import com.energiefixers.backend.property.service.FixRoundService;
import com.energiefixers.backend.shared.ApiResponse;
import com.energiefixers.backend.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fix-rounds")
@RequiredArgsConstructor
public class FixRoundController {

    private final FixRoundService fixRoundService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<FixRoundResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(fixRoundService.getAll()));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<FixRoundResponse>> getCurrent() {
        FixRoundResponse current = fixRoundService.getCurrentRound()
                .orElseThrow(() -> new NotFoundException("Geen huidige fixronde ingesteld."));
        return ResponseEntity.ok(ApiResponse.success(current));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<FixRoundResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(fixRoundService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FixRoundResponse>> create(@RequestBody FixRoundRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success(fixRoundService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FixRoundResponse>> update(
            @PathVariable Long id,
            @RequestBody FixRoundRequest request) {
        return ResponseEntity.ok(ApiResponse.success(fixRoundService.update(id, request)));
    }

    @PutMapping("/{id}/set-current")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FixRoundResponse>> setCurrent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(fixRoundService.setCurrent(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        fixRoundService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
