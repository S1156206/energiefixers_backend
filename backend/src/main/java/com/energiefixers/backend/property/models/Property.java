package com.energiefixers.backend.property.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.invitation.models.Invitation;
import com.energiefixers.backend.user.models.User;
import com.energiefixers.backend.visit.models.FixVisit;

/**
 * A property that has been (or will be) visited by Energiefixers071.
 * Deliberately separated from User so that a property can have multiple
 * tenants over time — social housing turns over regularly.
 */
@Entity
@Table(name = "property")
@Getter @Setter @NoArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String houseNumber;

    private String houseNumberSuffix;

    @Column(nullable = false, length = 6)
    private String postcode;

    /** Energy label before the fix visit, e.g. "E", "F", "G" */
    @Enumerated(EnumType.STRING)
    @Column(name = "energy_label_before")
    private EnergyLabel energyLabelBefore;

    /** Energy label after the fix visit (if known) */
    @Enumerated(EnumType.STRING)
    @Column(name = "energy_label_after")
    private EnergyLabel energyLabelAfter;

    @ManyToOne(optional = false)
    @JoinColumn(name = "region_id")
    private Region region;

    /** Current tenant who enters data via the dashboard */
    @OneToOne(mappedBy = "property")
    private User tenant;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<FixVisit> fixVisits;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<EnergyReading> energyReadings;

    @OneToMany(mappedBy = "property")
    private List<Invitation> invitations;

    public enum EnergyLabel {
        A_PLUS_PLUS_PLUS, A_PLUS_PLUS, A_PLUS, A, B, C, D, E, F, G
    }
}
