package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.Booking;
import com.fifa.fifarest.domain.BookingStatus;
import com.fifa.fifarest.domain.Court;
import com.fifa.fifarest.domain.Slot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record OwnerBookingResponse(
        Long id,
        Long venueId,
        String venueName,
        String courtName,
        String sportName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal amount,
        BookingStatus status,
        String playerName,
        String playerEmail,
        String playerPhone,
        Instant createdAt
) {
    public static OwnerBookingResponse from(Booking booking) {
        Slot slot = booking.getSlot();
        Court court = slot.getCourt();

        return new OwnerBookingResponse(
                booking.getId(),
                court.getVenue().getId(),
                court.getVenue().getName(),
                court.getName(),
                court.getSport().getName(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                booking.getAmount(),
                booking.getStatus(),
                booking.getPlayer().getFullName(),
                booking.getPlayer().getEmail(),
                booking.getPlayer().getPhone(),
                booking.getCreatedAt()
        );
    }
}
