package com.energiefixers.backend.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.energiefixers.backend.property.models.Region;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    @Query("SELECT r FROM Region r JOIN r.postcodePrefixes p WHERE :postcode LIKE CONCAT(p, '%')")
    Optional<Region> findByPostcode(@Param("postcode") String postcode);
}
