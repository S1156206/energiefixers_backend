package com.energiefixers.backend.property.service;

import com.energiefixers.backend.property.dto.FixRoundRequest;
import com.energiefixers.backend.property.dto.FixRoundResponse;
import com.energiefixers.backend.property.models.FixRound;
import com.energiefixers.backend.property.repository.FixRoundRepository;
import com.energiefixers.backend.property.repository.PropertyRepository;
import com.energiefixers.backend.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FixRoundService {

    private final FixRoundRepository fixRoundRepository;
    private final PropertyRepository propertyRepository;

    public List<FixRoundResponse> getAll() {
        return fixRoundRepository.findAllByOrderByStartDateDesc().stream()
                .map(r -> FixRoundResponse.from(r, propertyRepository.countByFixRoundId(r.getId())))
                .collect(Collectors.toList());
    }

    public Optional<FixRoundResponse> getCurrentRound() {
        return fixRoundRepository.findByCurrentTrue()
                .map(r -> FixRoundResponse.from(r, propertyRepository.countByFixRoundId(r.getId())));
    }

    public FixRoundResponse getById(Long id) {
        FixRound round = findByIdOrThrow(id);
        return FixRoundResponse.from(round, propertyRepository.countByFixRoundId(id));
    }

    @Transactional
    public FixRoundResponse create(FixRoundRequest request) {
        FixRound round = new FixRound();
        round.setName(request.getName());
        round.setDescription(request.getDescription());
        round.setStartDate(request.getStartDate());
        round.setEndDate(request.getEndDate());
        round.setCurrent(false);
        FixRound saved = fixRoundRepository.save(round);
        return FixRoundResponse.from(saved, 0);
    }

    @Transactional
    public FixRoundResponse update(Long id, FixRoundRequest request) {
        FixRound round = findByIdOrThrow(id);
        round.setName(request.getName());
        round.setDescription(request.getDescription());
        round.setStartDate(request.getStartDate());
        round.setEndDate(request.getEndDate());
        FixRound saved = fixRoundRepository.save(round);
        return FixRoundResponse.from(saved, propertyRepository.countByFixRoundId(id));
    }

    @Transactional
    public FixRoundResponse setCurrent(Long id) {
        FixRound target = findByIdOrThrow(id);
        List<FixRound> all = fixRoundRepository.findAll();
        all.forEach(r -> r.setCurrent(false));
        fixRoundRepository.saveAll(all);
        target.setCurrent(true);
        FixRound saved = fixRoundRepository.save(target);
        return FixRoundResponse.from(saved, propertyRepository.countByFixRoundId(id));
    }

    @Transactional
    public void delete(Long id) {
        findByIdOrThrow(id);
        long count = propertyRepository.countByFixRoundId(id);
        if (count > 0) {
            throw new IllegalStateException("Kan fixronde niet verwijderen: er zijn nog " + count + " woningen gekoppeld aan deze ronde.");
        }
        fixRoundRepository.deleteById(id);
    }

    private FixRound findByIdOrThrow(Long id) {
        return fixRoundRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixronde niet gevonden: " + id));
    }
}
