package com.energiefixers.backend.energy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.energiefixers.backend.energy.models.EnergyReading;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface EnergyReadingRepository extends JpaRepository<EnergyReading, Long> {
    List<EnergyReading> findAllByPropertyId(Long propertyId);
    List<EnergyReading> findAllByPropertyIdOrderByPeriodEndAsc(Long propertyId);
    List<EnergyReading> findAllByPropertyIdIn(Collection<Long> propertyIds);

    @Query("""
        SELECT r FROM EnergyReading r
        WHERE r.property.id = :propertyId
          AND r.periodStart < :periodEnd
          AND r.periodEnd > :periodStart
          AND (:excludeId IS NULL OR r.id <> :excludeId)
        """)
    List<EnergyReading> findOverlapping(
        @Param("propertyId") Long propertyId,
        @Param("periodStart") LocalDate periodStart,
        @Param("periodEnd") LocalDate periodEnd,
        @Param("excludeId") Long excludeId
    );
}
