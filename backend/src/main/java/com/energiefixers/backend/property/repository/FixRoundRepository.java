package com.energiefixers.backend.property.repository;

import com.energiefixers.backend.property.models.FixRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FixRoundRepository extends JpaRepository<FixRound, Long> {
    Optional<FixRound> findByCurrentTrue();
    List<FixRound> findAllByOrderByStartDateDesc();
}
