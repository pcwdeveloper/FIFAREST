package com.fifa.fifarest.service;

import com.fifa.fifarest.domain.User;
import com.fifa.fifarest.domain.Venue;
import com.fifa.fifarest.domain.VenueStatus;
import com.fifa.fifarest.dto.VenueRequest;
import com.fifa.fifarest.dto.VenueResponse;
import com.fifa.fifarest.dto.VenueStatusUpdateRequest;
import com.fifa.fifarest.exception.ForbiddenActionException;
import com.fifa.fifarest.exception.ResourceNotFoundException;
import com.fifa.fifarest.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VenueService {

    private final VenueRepository venueRepository;
    private final UserService userService;

    public VenueService(VenueRepository venueRepository, UserService userService) {
        this.venueRepository = venueRepository;
        this.userService = userService;
    }

    @Transactional
    public VenueResponse create(String ownerEmail, VenueRequest request) {
        User owner = userService.getByEmail(ownerEmail);

        Venue venue = Venue.builder()
                .name(request.name())
                .address(request.address())
                .city(request.city())
                .description(request.description())
                .owner(owner)
                .status(VenueStatus.PENDING)
                .build();

        return VenueResponse.from(venueRepository.save(venue));
    }

    public List<VenueResponse> listMine(String ownerEmail) {
        User owner = userService.getByEmail(ownerEmail);
        return venueRepository.findByOwner(owner).stream().map(VenueResponse::from).toList();
    }

    public List<VenueResponse> listAll(VenueStatus statusFilter) {
        List<Venue> venues = statusFilter != null
                ? venueRepository.findByStatus(statusFilter)
                : venueRepository.findAll();
        return venues.stream().map(VenueResponse::from).toList();
    }

    public List<VenueResponse> listPublic(String city) {
        return venueRepository.findByStatus(VenueStatus.APPROVED).stream()
                .filter(venue -> city == null || city.isBlank() || venue.getCity().equalsIgnoreCase(city))
                .map(VenueResponse::from)
                .toList();
    }

    public VenueResponse getForOwner(Long id, String requesterEmail) {
        Venue venue = getById(id);
        assertOwner(venue, requesterEmail);
        return VenueResponse.from(venue);
    }

    @Transactional
    public VenueResponse update(Long id, String requesterEmail, VenueRequest request) {
        Venue venue = getById(id);
        assertOwner(venue, requesterEmail);

        venue.setName(request.name());
        venue.setAddress(request.address());
        venue.setCity(request.city());
        venue.setDescription(request.description());

        return VenueResponse.from(venueRepository.save(venue));
    }

    @Transactional
    public void delete(Long id, String requesterEmail) {
        Venue venue = getById(id);
        assertOwner(venue, requesterEmail);
        venueRepository.delete(venue);
    }

    @Transactional
    public VenueResponse updateStatus(Long id, VenueStatusUpdateRequest request) {
        if (request.status() == VenueStatus.PENDING) {
            throw new IllegalArgumentException("Status can only be set to APPROVED or REJECTED");
        }

        Venue venue = getById(id);
        venue.setStatus(request.status());
        return VenueResponse.from(venueRepository.save(venue));
    }

    Venue getById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
    }

    private void assertOwner(Venue venue, String requesterEmail) {
        if (!venue.getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new ForbiddenActionException("You do not own this venue");
        }
    }
}
