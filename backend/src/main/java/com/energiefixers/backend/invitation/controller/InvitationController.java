package com.energiefixers.backend.invitation.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.energiefixers.backend.auth.dto.LoginRequest;
import com.energiefixers.backend.auth.dto.LoginResponse;
import com.energiefixers.backend.auth.service.AuthService;
import com.energiefixers.backend.invitation.dto.InvitationRequest;
import com.energiefixers.backend.invitation.dto.InvitationResponse;
import com.energiefixers.backend.invitation.dto.RegistrationRequest;
import com.energiefixers.backend.invitation.models.Invitation;
import com.energiefixers.backend.invitation.models.Invitation.InvitationType;
import com.energiefixers.backend.invitation.service.InvitationService;
import com.energiefixers.backend.shared.ApiResponse;
import com.energiefixers.backend.user.service.UserService;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;
    private final AuthService authService;

    /**
     * Staff sends an invitation to a tenant.
     * POST /api/invitations?propertyId=1&type=REGISTRATION
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvitationResponse>> send(@RequestBody InvitationRequest request) {
        Invitation invitation = invitationService.createInvitation(request);
        InvitationResponse response = InvitationResponse.from(invitation);
        response.setNextMailAvailableAt(invitation.getSentAt().plus(InvitationService.COOLDOWN));
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    /**
     * Tenant opens the invite link — validate the token before showing the register form.
     * GET /api/invitations/{token}
     */
    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<InvitationResponse>> validate(@PathVariable String token) {
        Invitation invitation = invitationService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(InvitationResponse.from(invitation)));
    }

    /**
     * Tenant submits the registration form.
     * POST /api/invitations/{token}/accept
     *
     * This is the only unauthenticated write endpoint — the token is the proof of identity.
     */
    @PostMapping("/{token}/accept")
    public ResponseEntity<ApiResponse<LoginResponse>> accept(@PathVariable String token,
                                                   @RequestBody RegistrationRequest request) {
        Invitation invitation = invitationService.validateToken(token);
        userService.registerFromInvitation(invitation, request);
        LoginResponse response = authService.login(new LoginRequest(request.getEmail(), request.getPassword()));
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }
}
