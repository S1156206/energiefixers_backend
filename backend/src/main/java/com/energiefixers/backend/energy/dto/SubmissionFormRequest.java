package com.energiefixers.backend.energy.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SubmissionFormRequest {
    private BigDecimal gasUsageM3;
    private BigDecimal electricityUsageKwh;
    private BigDecimal totalCostEuros;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
