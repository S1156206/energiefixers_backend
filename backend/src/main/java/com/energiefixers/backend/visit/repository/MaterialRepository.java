package com.energiefixers.backend.visit.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.energiefixers.backend.visit.models.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Query("SELECT COUNT(im) > 0 FROM InstalledMaterial im WHERE im.material.id = :id")
    boolean isUsedInAnyVisit(@Param("id") Long id);
    @Query("SELECT m.id, m.name, m.category, SUM(im.quantity), SUM(im.quantity * m.priceEuros) " +
       "FROM Material m LEFT JOIN InstalledMaterial im ON im.material = m " +
       "GROUP BY m.id, m.name, m.category")
    List<Object[]> getMaterialUsageStats();

    @Query("SELECT m.id, m.name, m.category, SUM(im.quantity), SUM(im.quantity * m.priceEuros) " +
           "FROM InstalledMaterial im JOIN im.material m JOIN im.fixVisit fv " +
           "WHERE (:regionId IS NULL OR fv.property.region.id = :regionId) " +
           "AND (:propertyId IS NULL OR fv.property.id = :propertyId) " +
           "AND (:from IS NULL OR fv.visitDate >= :from) " +
           "AND (:to IS NULL OR fv.visitDate <= :to) " +
           "GROUP BY m.id, m.name, m.category " +
           "ORDER BY SUM(im.quantity) DESC")
    List<Object[]> getMaterialUsageStatsFiltered(@Param("regionId") Long regionId,
                                                  @Param("propertyId") Long propertyId,
                                                  @Param("from") LocalDate from,
                                                  @Param("to") LocalDate to);
}
