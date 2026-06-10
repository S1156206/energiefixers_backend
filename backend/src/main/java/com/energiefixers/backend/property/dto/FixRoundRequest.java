package com.energiefixers.backend.property.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class FixRoundRequest {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
