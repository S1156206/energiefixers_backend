package com.energiefixers.backend.visit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.energiefixers.backend.visit.models.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    @Query("SELECT m.id, m.name, m.category, SUM(im.quantity), SUM(im.quantity * m.priceEuros) " +
       "FROM Material m LEFT JOIN InstalledMaterial im ON im.material = m " +
       "GROUP BY m.id, m.name, m.category")
List<Object[]> getMaterialUsageStats();
}
