package com.energiefixers.backend.property.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energiefixers.backend.property.dto.PropertyRequest;
import com.energiefixers.backend.property.models.FixRound;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.repository.FixRoundRepository;
import com.energiefixers.backend.property.repository.PropertyRepository;
import com.energiefixers.backend.property.repository.RegionRepository;
import com.energiefixers.backend.shared.NotFoundException;
import com.energiefixers.backend.user.models.User;
import com.energiefixers.backend.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final FixRoundRepository fixRoundRepository;

    public Property getMyProperty(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        if (user.getProperty() == null) {
            throw new NotFoundException("No property linked to this account.");
        }
        return user.getProperty();
    }

    public List<Property> getAll() {
        return propertyRepository.findAll();
    }

    public List<Property> getAllByRegion(Long regionId) {
        return propertyRepository.findAllByRegionId(regionId);
    }

    public List<Property> getAllByFixRound(Long fixRoundId) {
        return propertyRepository.findAllByFixRoundId(fixRoundId);
    }

    public Property getById(Long id) {
        return propertyRepository.findById(id).orElseThrow(() -> new NotFoundException("Property with id not found: "+ id));
        
    }

    @Transactional
    public Property create(PropertyRequest request) {
        Property property = new Property();
        property.setStreet(request.getStreet());
        property.setCity(request.getCity());
        property.setHouseNumber(request.getHouseNumber());
        property.setHouseNumberSuffix(request.getHouseNumberSuffix());
        property.setPostcode(request.getPostcode());
        // property.setEnergyLabelBefore(request.getEnergyLabelBefore());
        property.setTenantEmail(request.getTenantEmail());
        property.setRegion(regionRepository.findByPostcode(request.getPostcode()).orElse(null));

        if (request.getFixRoundId() != null) {
            FixRound round = fixRoundRepository.findById(request.getFixRoundId())
                    .orElseThrow(() -> new NotFoundException("Fixronde niet gevonden: " + request.getFixRoundId()));
            property.setFixRound(round);
        } else {
            fixRoundRepository.findByCurrentTrue().ifPresent(property::setFixRound);
        }

        return propertyRepository.save(property);
    }

    @Transactional
    public Property update(Long id, PropertyRequest request) {
        Property property = getById(id);

        property.setStreet(request.getStreet());
        property.setCity(request.getCity());
        property.setHouseNumber(request.getHouseNumber());
        property.setHouseNumberSuffix(request.getHouseNumberSuffix());
        property.setPostcode(request.getPostcode());
        // property.setEnergyLabelBefore(request.getEnergyLabelBefore());
        // property.setEnergyLabelAfter(request.getEnergyLabelAfter());
        property.setTenantEmail(request.getTenantEmail());
        property.setRegion(regionRepository.findByPostcode(request.getPostcode()).orElse(null));

        if (request.getFixRoundId() != null) {
            FixRound round = fixRoundRepository.findById(request.getFixRoundId())
                    .orElseThrow(() -> new NotFoundException("Fixronde niet gevonden: " + request.getFixRoundId()));
            property.setFixRound(round);
        } else {
            property.setFixRound(null);
        }

        return propertyRepository.save(property);
    }

    @Transactional
    public void delete(Long id) {
        Property property = getById(id);
        if (property.getTenant() != null) {
            User tenant = property.getTenant();
            tenant.setProperty(null);
            userRepository.save(tenant);
        }
        propertyRepository.deleteById(id);
    }
}
