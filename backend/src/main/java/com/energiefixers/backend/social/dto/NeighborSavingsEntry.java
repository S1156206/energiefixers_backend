package com.energiefixers.backend.social.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class NeighborSavingsEntry {
    private final boolean isYou;
    private final LocalDate fixVisitDate;
    private final BigDecimal estimatedGasSavingsM3;
    private final BigDecimal estimatedElectricitySavingsKwh;
    private final BigDecimal actualGasSavingsM3;
    private final BigDecimal actualElectricitySavingsKwh;
    private final BigDecimal actualCostSavingsEuros;
    private final boolean hasActualData;
}
