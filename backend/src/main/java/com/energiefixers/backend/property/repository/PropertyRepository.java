package com.energiefixers.backend.property.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energiefixers.backend.property.models.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findAllByRegionId(Long id);
    Long countByRegionId(Long regionId);
}
