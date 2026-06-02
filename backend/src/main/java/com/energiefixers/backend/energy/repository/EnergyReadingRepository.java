package com.energiefixers.backend.energy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.energiefixers.backend.energy.models.EnergyReading;

import java.util.Collection;
import java.util.List;

public interface EnergyReadingRepository extends JpaRepository<EnergyReading, Long> {
    List<EnergyReading> findAllByPropertyId(Long propertyId);
    List<EnergyReading> findAllByPropertyIdOrderByPeriodEndAsc(Long propertyId);
    List<EnergyReading> findAllByPropertyIdIn(Collection<Long> propertyIds);
}
