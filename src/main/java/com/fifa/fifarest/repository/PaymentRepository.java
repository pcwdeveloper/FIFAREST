package com.fifa.fifarest.repository;

import com.fifa.fifarest.domain.Booking;
import com.fifa.fifarest.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);
}
