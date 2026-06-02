package com.energiefixers.backend.energy.repository;

import com.energiefixers.backend.energy.models.PropertySubmissionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropertySubmissionRequestRepository extends JpaRepository<PropertySubmissionRequest, Long> {
    Optional<PropertySubmissionRequest> findByToken(String token);
    List<PropertySubmissionRequest> findAllByPropertyId(Long propertyId);
    List<PropertySubmissionRequest> findAllByPropertyIdAndSubmittedAtIsNull(Long propertyId);
}
