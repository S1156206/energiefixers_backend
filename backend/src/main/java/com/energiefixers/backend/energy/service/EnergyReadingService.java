package com.energiefixers.backend.energy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energiefixers.backend.energy.dto.EnergyReadingRequest;
import com.energiefixers.backend.energy.models.EnergyReading;
import com.energiefixers.backend.energy.repository.EnergyReadingRepository;
import com.energiefixers.backend.shared.NotFoundException;
import com.energiefixers.backend.user.models.User;
import com.energiefixers.backend.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnergyReadingService {

    private final EnergyReadingRepository energyReadingRepository;
    private final UserRepository userRepository;

    public List<EnergyReading> findForTenant(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getProperty() == null) {
            throw new IllegalStateException("Current user is not assigned to a property.");
        }

        return energyReadingRepository.findAllByPropertyId(user.getProperty().getId());
    }

    @Transactional
    public EnergyReading createForTenant(Long userId, EnergyReadingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getProperty() == null) {
            throw new IllegalStateException("Current user is not assigned to a property.");
        }

        EnergyReading reading = new EnergyReading();
        reading.setProperty(user.getProperty());
        reading.setPeriodStart(request.getPeriodStart());
        reading.setPeriodEnd(request.getPeriodEnd());
        reading.setGasUsageM3(request.getGasUsageM3());
        reading.setElectricityUsageKwh(request.getElectricityUsageKwh());
        reading.setTotalCostEuros(request.getTotalCostEuros());
        reading.setSourceType(EnergyReading.SourceType.ANNUAL_BILL_MANUAL);

        return energyReadingRepository.save(reading);
    }

    @Transactional
    public EnergyReading updateForTenant(Long userId, Long readingId, EnergyReadingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getProperty() == null) {
            throw new IllegalStateException("Current user is not assigned to a property.");
        }

        EnergyReading reading = energyReadingRepository.findById(readingId)
                .orElseThrow(() -> new NotFoundException("Energy reading not found: " + readingId));

        // Verify the reading belongs to the tenant's property
        if (!reading.getProperty().getId().equals(user.getProperty().getId())) {
            throw new IllegalStateException("You do not have permission to update this reading.");
        }

        reading.setPeriodStart(request.getPeriodStart());
        reading.setPeriodEnd(request.getPeriodEnd());
        reading.setGasUsageM3(request.getGasUsageM3());
        reading.setElectricityUsageKwh(request.getElectricityUsageKwh());
        reading.setTotalCostEuros(request.getTotalCostEuros());

        return energyReadingRepository.save(reading);
    }

    @Transactional
    public void deleteForTenant(Long userId, Long readingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getProperty() == null) {
            throw new IllegalStateException("Current user is not assigned to a property.");
        }

        EnergyReading reading = energyReadingRepository.findById(readingId)
                .orElseThrow(() -> new NotFoundException("Energy reading not found: " + readingId));

        if (!reading.getProperty().getId().equals(user.getProperty().getId())) {
            throw new IllegalStateException("You do not have permission to delete this reading.");
        }

        energyReadingRepository.deleteById(readingId);
    }
}
