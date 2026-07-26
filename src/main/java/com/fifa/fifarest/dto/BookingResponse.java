package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.Booking;
import com.fifa.fifarest.domain.BookingStatus;
import com.fifa.fifarest.domain.Court;
import com.fifa.fifarest.domain.Slot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record BookingResponse(
        Long id,
        Long slotId,
        Long courtId,
        String courtName,
        Long venueId,
        String venueName,
        String sportName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal amount,
        BookingStatus status,
        Instant createdAt
) {
    public static BookingResponse from(Booking booking) {
        Slot slot = booking.getSlot();
        Court court = slot.getCourt();

        return new BookingResponse(
                booking.getId(),
                slot.getId(),
                court.getId(),
                court.getName(),
                court.getVenue().getId(),
                court.getVenue().getName(),
                court.getSport().getName(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                booking.getAmount(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}
