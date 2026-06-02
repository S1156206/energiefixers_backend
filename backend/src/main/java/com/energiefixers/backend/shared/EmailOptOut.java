package com.energiefixers.backend.shared;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_opt_out")
@Getter @Setter @NoArgsConstructor
public class EmailOptOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "unsubscribe_token", nullable = false, unique = true)
    private String unsubscribeToken;

    @Column(name = "opted_out_at")
    private LocalDateTime optedOutAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.unsubscribeToken = UUID.randomUUID().toString();
    }

    public boolean isOptedOut() {
        return this.optedOutAt != null;
    }
}
