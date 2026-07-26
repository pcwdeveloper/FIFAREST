package com.fifa.fifarest.repository;

import com.fifa.fifarest.domain.Court;
import com.fifa.fifarest.domain.Slot;
import com.fifa.fifarest.domain.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByCourtOrderByDateAscStartTimeAsc(Court court);

    List<Slot> findByCourtAndDate(Court court, LocalDate date);

    List<Slot> findByCourtAndDateAndStatusOrderByStartTimeAsc(Court court, LocalDate date, SlotStatus status);
}
