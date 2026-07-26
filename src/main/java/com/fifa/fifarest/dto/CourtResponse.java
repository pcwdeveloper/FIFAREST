package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.Court;

import java.math.BigDecimal;

public record CourtResponse(
        Long id,
        String name,
        Long venueId,
        Long sportId,
        String sportName,
        BigDecimal pricePerSlot,
        boolean active
) {
    public static CourtResponse from(Court court) {
        return new CourtResponse(
                court.getId(),
                court.getName(),
                court.getVenue().getId(),
                court.getSport().getId(),
                court.getSport().getName(),
                court.getPricePerSlot(),
                court.isActive()
        );
    }
}
