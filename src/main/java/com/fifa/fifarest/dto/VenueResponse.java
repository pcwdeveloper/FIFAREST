package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.Venue;
import com.fifa.fifarest.domain.VenueStatus;

import java.time.Instant;

public record VenueResponse(
        Long id,
        String name,
        String address,
        String city,
        String description,
        String thumbnailFileName,
        VenueStatus status,
        Long ownerId,
        String ownerName,
        Instant createdAt
) {
    public static VenueResponse from(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getAddress(),
                venue.getCity(),
                venue.getDescription(),
                venue.getThumbnailFileName(),
                venue.getStatus(),
                venue.getOwner().getId(),
                venue.getOwner().getFullName(),
                venue.getCreatedAt()
        );
    }
}
