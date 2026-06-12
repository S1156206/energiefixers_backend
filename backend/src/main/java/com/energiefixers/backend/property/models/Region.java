package com.energiefixers.backend.property.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /** Postcode prefixes for automatic region assignment, e.g. {"2316", "2317"} */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "region_postcode_prefix", joinColumns = @JoinColumn(name = "region_id"))
    @Column(name = "prefix")
    private Set<String> postcodePrefixes = new HashSet<>();

    @OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
    private List<Property> properties;
}
