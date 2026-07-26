package com.fifa.fifarest.repository;

import com.fifa.fifarest.domain.Booking;
import com.fifa.fifarest.domain.BookingStatus;
import com.fifa.fifarest.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByPlayerOrderByCreatedAtDesc(User player);

    List<Booking> findBySlot_Court_Venue_OwnerOrderByCreatedAtDesc(User owner);

    long countByStatus(BookingStatus status);

    long countBySlot_Court_Venue_Owner(User owner);

    long countBySlot_Court_Venue_OwnerAndStatus(User owner, BookingStatus status);

    @Query("select coalesce(sum(b.amount), 0) from Booking b where b.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") BookingStatus status);

    @Query("select coalesce(sum(b.amount), 0) from Booking b where b.slot.court.venue.owner = :owner and b.status = :status")
    BigDecimal sumAmountByOwnerAndStatus(@Param("owner") User owner, @Param("status") BookingStatus status);
}
