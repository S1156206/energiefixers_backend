package com.energiefixers.backend.visit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.repository.PropertyRepository;
import com.energiefixers.backend.shared.NotFoundException;
import com.energiefixers.backend.visit.dto.FixVisitRequest;
import com.energiefixers.backend.visit.dto.InstalledMaterialRequest;
import com.energiefixers.backend.visit.models.FixVisit;
import com.energiefixers.backend.visit.models.InstalledMaterial;
import com.energiefixers.backend.visit.models.Material;
import com.energiefixers.backend.visit.repository.FixVisitRepository;
import com.energiefixers.backend.visit.repository.MaterialRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FixVisitService {

    private final FixVisitRepository fixVisitRepository;
    private final PropertyRepository propertyRepository;
    private final MaterialRepository materialRepository;

    @Transactional
    public FixVisit addFixVisit(Long propertyId, FixVisitRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property not found: " + propertyId));

        FixVisit visit = new FixVisit();
        visit.setProperty(property);
        visit.setVisitDate(request.getVisitDate());
        visit.setNotes(request.getNotes());
        visit.setTotalMaterialCost(request.getTotalMaterialCost());

        List<InstalledMaterial> installedMaterials = new ArrayList<>();
        if (request.getInstalledMaterials() != null) {
            for (InstalledMaterialRequest item : request.getInstalledMaterials()) {
                if (item.getMaterialId() == null) {
                    throw new IllegalArgumentException("Installed material must include materialId.");
                }
                if (item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Installed material quantity must be greater than zero.");
                }

                Material material = materialRepository.findById(item.getMaterialId())
                        .orElseThrow(() -> new NotFoundException("Material not found: " + item.getMaterialId()));

                InstalledMaterial installed = new InstalledMaterial();
                installed.setFixVisit(visit);
                installed.setMaterial(material);
                installed.setQuantity(item.getQuantity());
                installedMaterials.add(installed);
            }
        }

        visit.setInstalledMaterials(installedMaterials);

        return fixVisitRepository.save(visit);
    }

    @Transactional
    public FixVisit updateFixVisit(Long propertyId, Long visitId, FixVisitRequest request) {
        FixVisit visit = fixVisitRepository.findById(visitId)
                .orElseThrow(() -> new NotFoundException("Fix visit not found: " + visitId));
        if (!visit.getProperty().getId().equals(propertyId)) {
            throw new NotFoundException("Fix visit not found for this property");
        }

        visit.setVisitDate(request.getVisitDate());
        visit.setNotes(request.getNotes());
        visit.setTotalMaterialCost(request.getTotalMaterialCost());

        visit.getInstalledMaterials().clear();
        if (request.getInstalledMaterials() != null) {
            for (InstalledMaterialRequest item : request.getInstalledMaterials()) {
                if (item.getMaterialId() == null) {
                    throw new IllegalArgumentException("Installed material must include materialId.");
                }
                if (item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Installed material quantity must be greater than zero.");
                }

                Material material = materialRepository.findById(item.getMaterialId())
                        .orElseThrow(() -> new NotFoundException("Material not found: " + item.getMaterialId()));

                InstalledMaterial installed = new InstalledMaterial();
                installed.setFixVisit(visit);
                installed.setMaterial(material);
                installed.setQuantity(item.getQuantity());
                visit.getInstalledMaterials().add(installed);
            }
        }

        return fixVisitRepository.save(visit);
    }

    @Transactional
    public void deleteFixVisit(Long propertyId, Long visitId) {
        FixVisit visit = fixVisitRepository.findById(visitId)
                .orElseThrow(() -> new NotFoundException("Fix visit not found: " + visitId));
        if (!visit.getProperty().getId().equals(propertyId)) {
            throw new NotFoundException("Fix visit not found for this property");
        }
        fixVisitRepository.deleteById(visitId);
    }
}
