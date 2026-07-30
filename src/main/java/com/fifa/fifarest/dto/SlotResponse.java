package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.Slot;
import com.fifa.fifarest.domain.SlotStatus;
import com.fifa.fifarest.domain.TimeOfDay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record SlotResponse(
        Long id,
        Long courtId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal price,
        TimeOfDay timeOfDay,
        SlotStatus status
) {
    public static SlotResponse from(Slot slot) {
        BigDecimal price = slot.getPrice() != null ? slot.getPrice() : slot.getCourt().getPricePerSlot();
        TimeOfDay timeOfDay = slot.getCategory() != null ? slot.getCategory() : TimeOfDay.fromStartTime(slot.getStartTime());
        return new SlotResponse(
                slot.getId(),
                slot.getCourt().getId(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                price,
                timeOfDay,
                slot.getStatus()
        );
    }
}
