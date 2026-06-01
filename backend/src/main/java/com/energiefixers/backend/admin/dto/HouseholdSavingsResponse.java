package com.energiefixers.backend.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class HouseholdSavingsResponse {
    private Long propertyId;
    private String street;
    private String houseNumber;
    private String postcode;
    private LocalDate visitDate;
    private BigDecimal totalMaterialCost;
    private BigDecimal estimatedGasSavingsM3;
    private BigDecimal estimatedElectricitySavingsKwh;
    private BigDecimal actualGasSavingsM3;
    private BigDecimal actualElectricitySavingsKwh;
}
