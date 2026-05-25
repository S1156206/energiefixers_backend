package com.energiefixers.backend.visit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.energiefixers.backend.visit.models.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {
}
