package com.energiefixers.backend.property.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energiefixers.backend.property.dto.PropertyRequest;
import com.energiefixers.backend.property.models.Property;
import com.energiefixers.backend.property.models.Region;
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

    public Property getById(Long id) {
        return propertyRepository.findById(id).orElseThrow(() -> new NotFoundException("Property with id not found: "+ id));
        
    }

    @Transactional
    public Property create(PropertyRequest request) {
        Region region = regionRepository.findById(request.getRegionId()).orElseThrow(() -> new NotFoundException("Region with id not found: " + request.getRegionId()));

        Property property = new Property();
        property.setStreet(request.getStreet());
        property.setHouseNumber(request.getHouseNumber());
        property.setHouseNumberSuffix(request.getHouseNumberSuffix());
        property.setPostcode(request.getPostcode());
        property.setEnergyLabelBefore(request.getEnergyLabelBefore());
        property.setRegion(region);

       return propertyRepository.save(property);
    }

    @Transactional
    public Property update(Long id, PropertyRequest request) {
        Property property = getById(id);

        property.setStreet(request.getStreet());
        property.setHouseNumber(request.getHouseNumber());
        property.setHouseNumberSuffix(request.getHouseNumberSuffix());
        property.setPostcode(request.getPostcode());
        property.setEnergyLabelBefore(request.getEnergyLabelBefore());
        property.setEnergyLabelAfter(request.getEnergyLabelAfter());

        if (request.getRegionId() != null) {
            Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new NotFoundException("Region not found: " + request.getRegionId()));
            property.setRegion(region);
        }

        return propertyRepository.save(property);
    }
}
