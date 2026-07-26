package com.fifa.fifarest.web;

import com.fifa.fifarest.domain.VenueStatus;
import com.fifa.fifarest.dto.VenueRequest;
import com.fifa.fifarest.dto.VenueResponse;
import com.fifa.fifarest.dto.VenueStatusUpdateRequest;
import com.fifa.fifarest.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<VenueResponse> create(Authentication authentication, @Valid @RequestBody VenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.create(authentication.getName(), request));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<List<VenueResponse>> listMine(Authentication authentication) {
        return ResponseEntity.ok(venueService.listMine(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<VenueResponse> getMine(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(venueService.getForOwner(id, authentication.getName()));
    }

    @GetMapping("/public")
    public ResponseEntity<List<VenueResponse>> listPublic(@RequestParam(required = false) String city) {
        return ResponseEntity.ok(venueService.listPublic(city));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<VenueResponse> update(Authentication authentication, @PathVariable Long id,
                                                 @Valid @RequestBody VenueRequest request) {
        return ResponseEntity.ok(venueService.update(id, authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        venueService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VenueResponse>> listAll(@RequestParam(required = false) VenueStatus status) {
        return ResponseEntity.ok(venueService.listAll(status));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody VenueStatusUpdateRequest request) {
        return ResponseEntity.ok(venueService.updateStatus(id, request));
    }
}
