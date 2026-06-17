package com.energiefixers.backend.energy.service;

import com.energiefixers.backend.energy.models.EnergyReading;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;

@Component
public class SavingsCalculator {

    public BigDecimal averageDailyRate(List<EnergyReading> readings,
                                       Function<EnergyReading, BigDecimal> field) {
        List<BigDecimal> rates = readings.stream()
                .filter(r -> field.apply(r) != null)
                .filter(r -> periodDays(r) > 0)
                .map(r -> field.apply(r).divide(
                        BigDecimal.valueOf(periodDays(r)), 10, RoundingMode.HALF_UP))
                .toList();

        if (rates.isEmpty()) return null;

        BigDecimal sum = rates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(rates.size()), 10, RoundingMode.HALF_UP);
    }

    public BigDecimal annualize(BigDecimal dailyRate) {
        if (dailyRate == null) return null;
        return dailyRate.multiply(BigDecimal.valueOf(365)).setScale(2, RoundingMode.HALF_UP);
    }

    private long periodDays(EnergyReading r) {
        return ChronoUnit.DAYS.between(r.getPeriodStart(), r.getPeriodEnd());
    }
}
