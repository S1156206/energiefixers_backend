package com.energiefixers.backend.user.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.energiefixers.backend.property.models.Property;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String firstName;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Link to the tenant's property.
     * Null for Energiefixers staff / admins.
     */
    @OneToOne
    @JoinColumn(name = "property_id", unique = true)
    private Property property;

    /**
     * Total points earned for gamification.
     * Incremented when entering readings, earning badges, etc.
     */
    @Column(name = "gamification_points", nullable = false)
    private int gamificationPoints = 0;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }

}
