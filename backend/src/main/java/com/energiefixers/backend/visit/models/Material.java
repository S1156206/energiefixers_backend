package com.energiefixers.backend.visit.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A type of energy-saving material installed by Energiefixers071.
 * Examples: radiator foil, LED bulb, water-saving showerhead.
 * The estimated savings per unit are stored here so a projection
 * can be shown before the tenant submits their annual bill.
 */
@Entity
@Table(name = "material")
@Getter @Setter @NoArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    /** Purchase price in euros, towards the €200 per-property cap */
    @Column(name = "price_euros", precision = 6, scale = 2)
    private BigDecimal priceEuros;

    /** Estimated annual gas savings in m³ */
    @Column(name = "estimated_gas_saving_m3", precision = 8, scale = 2)
    private BigDecimal estimatedGasSavingM3;

    /** Estimated annual electricity savings in kWh */
    @Column(name = "estimated_electricity_saving_kwh", precision = 8, scale = 2)
    private BigDecimal estimatedElectricitySavingKwh;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit")
    private Unit unit;

    public enum Category {
        INSULATION,     // radiator foil, draught strips
        LIGHTING,       // LED bulbs
        WATER,          // water-saving showerhead
        VENTILATION,    // radiator fan
        OTHER
    }

    public enum Unit {
        PIECE,           
        METER,          
        SQUARE_METER, 
        ROLL             
    }
}
