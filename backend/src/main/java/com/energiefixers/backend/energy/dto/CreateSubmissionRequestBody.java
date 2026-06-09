package com.energiefixers.backend.energy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSubmissionRequestBody {
    private Long propertyId;
    private String email;
}
