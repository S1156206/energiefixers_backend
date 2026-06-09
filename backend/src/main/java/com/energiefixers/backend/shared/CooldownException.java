package com.energiefixers.backend.shared;

import java.time.LocalDateTime;

public class CooldownException extends RuntimeException {

    private final LocalDateTime nextAvailableAt;

    public CooldownException(LocalDateTime nextAvailableAt) {
        super("Too many requests. Next email can be sent at: " + nextAvailableAt);
        this.nextAvailableAt = nextAvailableAt;
    }

    public LocalDateTime getNextAvailableAt() {
        return nextAvailableAt;
    }
}
