package com.energiefixers.backend.invitation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistrationRequest {
    private String email;
    private String password;
    private String firstName;
}
