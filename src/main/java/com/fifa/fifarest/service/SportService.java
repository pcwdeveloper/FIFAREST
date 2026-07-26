package com.fifa.fifarest.service;

import com.fifa.fifarest.domain.Sport;
import com.fifa.fifarest.dto.SportRequest;
import com.fifa.fifarest.dto.SportResponse;
import com.fifa.fifarest.exception.DuplicateResourceException;
import com.fifa.fifarest.exception.ResourceNotFoundException;
import com.fifa.fifarest.repository.SportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SportService {

    private final SportRepository sportRepository;

    public SportService(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    public List<SportResponse> list() {
        return sportRepository.findAll().stream().map(SportResponse::from).toList();
    }

    @Transactional
    public SportResponse create(SportRequest request) {
        if (sportRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("A sport named '" + request.name() + "' already exists");
        }

        Sport sport = Sport.builder().name(request.name()).build();
        return SportResponse.from(sportRepository.save(sport));
    }

    @Transactional
    public SportResponse update(Long id, SportRequest request) {
        Sport sport = getById(id);

        sportRepository.findByNameIgnoreCase(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("A sport named '" + request.name() + "' already exists");
                });

        sport.setName(request.name());
        return SportResponse.from(sportRepository.save(sport));
    }

    @Transactional
    public void delete(Long id) {
        Sport sport = getById(id);
        sportRepository.delete(sport);
    }

    private Sport getById(Long id) {
        return sportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sport not found with id: " + id));
    }
}
