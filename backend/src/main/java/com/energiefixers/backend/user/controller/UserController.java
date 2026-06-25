package com.energiefixers.backend.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.energiefixers.backend.shared.ApiResponse;
import com.energiefixers.backend.shared.EmailOptOutService;
import com.energiefixers.backend.user.models.User;
import com.energiefixers.backend.user.repository.UserRepository;
import com.energiefixers.backend.shared.NotFoundException;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT')")
public class UserController {

    private final UserRepository userRepository;
    private final EmailOptOutService emailOptOutService;

    @GetMapping("/me/email-preference")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmailPreference(Authentication authentication) {
        User user = getUser(authentication);
        boolean optedOut = emailOptOutService.isOptedOut(user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "email", user.getEmail(),
            "optOut", optedOut
        )));
    }

    @PostMapping("/me/email-preference")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateEmailPreference(
            @RequestBody Map<String, Boolean> body,
            Authentication authentication) {
        Boolean optOutValue = body.get("optOut");
        if (optOutValue == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("optOut field is required"));
        }
        User user = getUser(authentication);
        boolean optOut = optOutValue;

        if (optOut) {
            emailOptOutService.optOutByEmail(user.getEmail());
        } else {
            emailOptOutService.optInByEmail(user.getEmail());
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "email", user.getEmail(),
            "optOut", optOut
        )));
    }

    private User getUser(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Long extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Number) {
            return ((Number) principal).longValue();
        }
        if (principal instanceof String) {
            return Long.parseLong((String) principal);
        }
        throw new IllegalStateException("Unable to determine current user id from authentication principal.");
    }
}
