package com.energiefixers.backend.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unsubscribe")
@RequiredArgsConstructor
public class UnsubscribeController {

    private final EmailOptOutService emailOptOutService;

    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(@PathVariable String token) {
        emailOptOutService.optOut(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
