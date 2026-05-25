package com.energiefixers.backend.energy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.energiefixers.backend.property.models.Property;

/**
 * One annual bill or energy reading entered by the tenant.
 * Actual savings are calculated by comparing two readings
 * (before and after the fix visit).
 *
 * GDPR note: data is linked to a property, not directly to a person.
 * Anonymous neighbourhood comparisons only expose aggregated
 * region-level data, never individual property data.
 */
@Entity
@Table(name = "energy_reading", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"property_id", "period_start", "period_end"}, name = "uk_energy_reading_property_period")
})
@Getter @Setter @NoArgsConstructor
public class EnergyReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id")
    private Property property;

    /** Start of the period covered by this annual bill */
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    /** End of the period covered by this annual bill */
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    /** Gas consumption in m³ over the period */
    @Column(name = "gas_usage_m3", precision = 10, scale = 2)
    private BigDecimal gasUsageM3;

    /** Electricity consumption in kWh over the period */
    @Column(name = "electricity_usage_kwh", precision = 10, scale = 2)
    private BigDecimal electricityUsageKwh;

    /** Total energy costs in euros over the period */
    @Column(name = "total_cost_euros", precision = 8, scale = 2)
    private BigDecimal totalCostEuros;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    /** When the tenant submitted this reading */
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }

    public enum SourceType {
        /** Tenant manually entered their annual bill */
        ANNUAL_BILL_MANUAL,
        /** Entered by a staff member */
        STAFF_ENTRY
    }
}
