package com.energiefixers.backend.visit.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Join table between FixVisit and Material, including quantity.
 * E.g. "3 LED bulbs" or "1 water-saving showerhead".
 */
@Entity
@Table(name = "installed_material")
@Getter @Setter @NoArgsConstructor
public class InstalledMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fix_visit_id")
    private FixVisit fixVisit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(nullable = false)
    private int quantity;
}
