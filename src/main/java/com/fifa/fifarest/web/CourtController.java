package com.fifa.fifarest.web;

import com.fifa.fifarest.dto.CourtRequest;
import com.fifa.fifarest.dto.CourtResponse;
import com.fifa.fifarest.service.CourtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CourtController {

    private final CourtService courtService;

    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @PostMapping("/api/venues/{venueId}/courts")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<CourtResponse> create(Authentication authentication, @PathVariable Long venueId,
                                                 @Valid @RequestBody CourtRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courtService.create(venueId, authentication.getName(), request));
    }

    @GetMapping("/api/venues/{venueId}/courts")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<List<CourtResponse>> listByVenue(Authentication authentication, @PathVariable Long venueId) {
        return ResponseEntity.ok(courtService.listByVenue(venueId, authentication.getName()));
    }

    @GetMapping("/api/venues/{venueId}/sports/{sportId}/courts")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<List<CourtResponse>> listByVenueAndSport(Authentication authentication,
                                                                     @PathVariable Long venueId,
                                                                     @PathVariable Long sportId) {
        return ResponseEntity.ok(courtService.listByVenueAndSport(venueId, sportId, authentication.getName()));
    }

    @GetMapping("/api/venues/{venueId}/courts/public")
    public ResponseEntity<List<CourtResponse>> listPublicByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(courtService.listPublicByVenue(venueId));
    }

    @PutMapping("/api/courts/{id}")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<CourtResponse> update(Authentication authentication, @PathVariable Long id,
                                                 @Valid @RequestBody CourtRequest request) {
        return ResponseEntity.ok(courtService.update(id, authentication.getName(), request));
    }

    @DeleteMapping("/api/courts/{id}")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        courtService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
