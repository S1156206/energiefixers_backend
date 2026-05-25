package com.energiefixers.backend.energy.dto;

import lombok.Getter;
import lombok.Setter;
import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.models.EnergyReading.SourceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class EnergyReadingResponse {
    private Long id;
    private Long propertyId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal gasUsageM3;
    private BigDecimal electricityUsageKwh;
    private BigDecimal totalCostEuros;
    private SourceType sourceType;
    private LocalDateTime submittedAt;

    public static EnergyReadingResponse from(EnergyReading reading) {
        EnergyReadingResponse response = new EnergyReadingResponse();
        response.setId(reading.getId());
        response.setPropertyId(reading.getProperty().getId());
        response.setPeriodStart(reading.getPeriodStart());
        response.setPeriodEnd(reading.getPeriodEnd());
        response.setGasUsageM3(reading.getGasUsageM3());
        response.setElectricityUsageKwh(reading.getElectricityUsageKwh());
        response.setTotalCostEuros(reading.getTotalCostEuros());
        response.setSourceType(reading.getSourceType());
        response.setSubmittedAt(reading.getSubmittedAt());
        return response;
    }
}
