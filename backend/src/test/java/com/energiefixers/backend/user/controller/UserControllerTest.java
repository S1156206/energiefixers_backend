package com.energiefixers.backend.user.controller;

import com.energiefixers.backend.shared.EmailOptOutService;
import com.energiefixers.backend.user.models.User;
import com.energiefixers.backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailOptOutService emailOptOutService;

    @MockitoBean
    private UserRepository userRepository;

    private User tenantUser;

    @BeforeEach
    void setUp() {
        tenantUser = new User();
        tenantUser.setId(1L);
        tenantUser.setEmail("tenant@test.com");

        SecurityContextHolder.clearContext();
        var auth = new UsernamePasswordAuthenticationToken(
            1L, null, List.of(new SimpleGrantedAuthority("ROLE_TENANT")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById(1L)).thenReturn(Optional.of(tenantUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getEmailPreference_returnsOptedOutStatus() throws Exception {
        when(emailOptOutService.isOptedOut("tenant@test.com")).thenReturn(true);

        mockMvc.perform(get("/api/users/me/email-preference"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.optOut").value(true));
    }

    @Test
    void getEmailPreference_returnsFalseWhenNotOptedOut() throws Exception {
        when(emailOptOutService.isOptedOut("tenant@test.com")).thenReturn(false);

        mockMvc.perform(get("/api/users/me/email-preference"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.optOut").value(false));
    }

    @Test
    void updateEmailPreference_callsOptOutService() throws Exception {
        Map<String, Boolean> body = Map.of("optOut", true);

        mockMvc.perform(post("/api/users/me/email-preference")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.optOut").value(true));

        verify(emailOptOutService).optOutByEmail("tenant@test.com");
    }

    @Test
    void updateEmailPreference_callsOptInService() throws Exception {
        Map<String, Boolean> body = Map.of("optOut", false);

        mockMvc.perform(post("/api/users/me/email-preference")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.optOut").value(false));

        verify(emailOptOutService).optInByEmail("tenant@test.com");
    }

    @Test
    void updateEmailPreference_returns400WhenOptOutMissing() throws Exception {
        Map<String, Object> body = Map.of();

        mockMvc.perform(post("/api/users/me/email-preference")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("optOut field is required"));
    }

    @Test
    void updateEmailPreference_returns400WhenOptOutIsNull() throws Exception {
        String body = "{\"optOut\": null}";

        mockMvc.perform(post("/api/users/me/email-preference")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("optOut field is required"));
    }

    @Test
    void updateEmailPreference_returns403ForStaffRole() throws Exception {
        SecurityContextHolder.clearContext();
        var auth = new UsernamePasswordAuthenticationToken(
            2L, null, List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Map<String, Boolean> body = Map.of("optOut", true);

        mockMvc.perform(post("/api/users/me/email-preference")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isForbidden());
    }
}
