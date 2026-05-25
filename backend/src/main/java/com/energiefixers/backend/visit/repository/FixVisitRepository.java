package com.energiefixers.backend.visit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.energiefixers.backend.visit.models.FixVisit;

import java.util.List;

public interface FixVisitRepository extends JpaRepository<FixVisit, Long> {
    List<FixVisit> findAllByPropertyId(Long propertyId);
}
