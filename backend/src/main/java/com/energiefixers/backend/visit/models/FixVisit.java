package com.energiefixers.backend.visit.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.energiefixers.backend.property.models.Property;

/**
 * A single visit by Energiefixers071 to a property.
 * Records which materials were installed and the total material cost.
 * This is also the baseline for savings calculations: the EnergyReading
 * before this visit is compared to the reading one year later.
 */
@Entity
@Table(name = "fix_visit")
@Getter @Setter @NoArgsConstructor
public class FixVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Sum of all installed materials, max €200 per property */
    @Column(name = "total_material_cost", precision = 7, scale = 2)
    private BigDecimal totalMaterialCost;

    @OneToMany(mappedBy = "fixVisit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstalledMaterial> installedMaterials;
}
