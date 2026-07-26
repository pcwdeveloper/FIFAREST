package com.fifa.fifarest.service;

import com.fifa.fifarest.domain.Court;
import com.fifa.fifarest.domain.Sport;
import com.fifa.fifarest.domain.Venue;
import com.fifa.fifarest.domain.VenueStatus;
import com.fifa.fifarest.dto.CourtRequest;
import com.fifa.fifarest.dto.CourtResponse;
import com.fifa.fifarest.exception.ForbiddenActionException;
import com.fifa.fifarest.exception.ResourceNotFoundException;
import com.fifa.fifarest.repository.CourtRepository;
import com.fifa.fifarest.repository.SportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourtService {

    private final CourtRepository courtRepository;
    private final SportRepository sportRepository;
    private final VenueService venueService;

    public CourtService(CourtRepository courtRepository, SportRepository sportRepository, VenueService venueService) {
        this.courtRepository = courtRepository;
        this.sportRepository = sportRepository;
        this.venueService = venueService;
    }

    @Transactional
    public CourtResponse create(Long venueId, String requesterEmail, CourtRequest request) {
        Venue venue = venueService.getById(venueId);
        assertOwner(venue, requesterEmail);
        Sport sport = getSport(request.sportId());

        Court court = Court.builder()
                .name(request.name())
                .venue(venue)
                .sport(sport)
                .pricePerSlot(request.pricePerSlot())
                .active(true)
                .build();

        return CourtResponse.from(courtRepository.save(court));
    }

    public List<CourtResponse> listByVenue(Long venueId, String requesterEmail) {
        Venue venue = venueService.getById(venueId);
        assertOwner(venue, requesterEmail);
        return courtRepository.findByVenue(venue).stream().map(CourtResponse::from).toList();
    }

    public List<CourtResponse> listPublicByVenue(Long venueId) {
        Venue venue = venueService.getById(venueId);
        if (venue.getStatus() != VenueStatus.APPROVED) {
            throw new ResourceNotFoundException("Venue not found with id: " + venueId);
        }
        return courtRepository.findByVenue(venue).stream()
                .filter(Court::isActive)
                .map(CourtResponse::from)
                .toList();
    }

    @Transactional
    public CourtResponse update(Long courtId, String requesterEmail, CourtRequest request) {
        Court court = getById(courtId);
        assertOwner(court.getVenue(), requesterEmail);

        court.setName(request.name());
        court.setSport(getSport(request.sportId()));
        court.setPricePerSlot(request.pricePerSlot());

        return CourtResponse.from(courtRepository.save(court));
    }

    @Transactional
    public void delete(Long courtId, String requesterEmail) {
        Court court = getById(courtId);
        assertOwner(court.getVenue(), requesterEmail);
        courtRepository.delete(court);
    }

    Court getById(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + id));
    }

    Court getByIdForOwner(Long id, String requesterEmail) {
        Court court = getById(id);
        assertOwner(court.getVenue(), requesterEmail);
        return court;
    }

    Court getByIdForPublicBrowse(Long id) {
        Court court = getById(id);
        if (court.getVenue().getStatus() != VenueStatus.APPROVED || !court.isActive()) {
            throw new ResourceNotFoundException("Court not found with id: " + id);
        }
        return court;
    }

    private Sport getSport(Long sportId) {
        return sportRepository.findById(sportId)
                .orElseThrow(() -> new ResourceNotFoundException("Sport not found with id: " + sportId));
    }

    private void assertOwner(Venue venue, String requesterEmail) {
        if (!venue.getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new ForbiddenActionException("You do not own this venue");
        }
    }
}
