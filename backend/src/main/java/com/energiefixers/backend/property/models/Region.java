package com.energiefixers.backend.property.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a district or neighbourhood in Leiden.
 * Used for municipality reporting and anonymous
 * neighbourhood comparisons (gamification).
 */
@Entity
@Table(name = "region")
@Getter @Setter @NoArgsConstructor
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** District name, e.g. "Leiden-Noord" or "Morsdistrict" */
    @Column(nullable = false, unique = true)
    private String name;

    /** Postcode prefix for automatic assignment, e.g. "2316" */
    @Column(name = "postcode_prefix")
    private String postcodePrefix;

    @OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
    private List<Property> properties;
}
