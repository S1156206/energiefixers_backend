package com.energiefixers.backend.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energiefixers.backend.property.models.Region;

public interface RegionRepository extends JpaRepository<Region, Long>{
}
