package com.energiefixers.backend.energy.controller;

import com.energiefixers.backend.energy.dto.SubmissionFormRequest;
import com.energiefixers.backend.energy.dto.SubmissionInfoResponse;
import com.energiefixers.backend.energy.dto.SubmissionResultResponse;
import com.energiefixers.backend.energy.service.SubmissionService;
import com.energiefixers.backend.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<SubmissionInfoResponse>> getInfo(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.success(submissionService.getSubmissionInfo(token)));
    }

    @PostMapping("/{token}")
    public ResponseEntity<ApiResponse<SubmissionResultResponse>> submit(
            @PathVariable String token,
            @RequestBody SubmissionFormRequest body) {
        return ResponseEntity.ok(ApiResponse.success(submissionService.submitReading(token, body)));
    }
}
