package com.fifa.fifarest.web;

import com.fifa.fifarest.dto.BookingInitResponse;
import com.fifa.fifarest.dto.BookingResponse;
import com.fifa.fifarest.dto.CreateBookingRequest;
import com.fifa.fifarest.dto.OwnerBookingResponse;
import com.fifa.fifarest.dto.VerifyPaymentRequest;
import com.fifa.fifarest.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/api/bookings")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<BookingInitResponse> create(Authentication authentication,
                                                        @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(authentication.getName(), request));
    }

    @GetMapping("/api/bookings/mine")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<List<BookingResponse>> listMine(Authentication authentication) {
        return ResponseEntity.ok(bookingService.listMine(authentication.getName()));
    }

    @PostMapping("/api/bookings/{id}/verify-payment")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<BookingResponse> verifyPayment(Authentication authentication, @PathVariable Long id,
                                                          @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(bookingService.verifyPayment(id, authentication.getName(), request));
    }

    @PostMapping("/api/bookings/{id}/cancel")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> cancel(Authentication authentication, @PathVariable Long id) {
        bookingService.cancelBooking(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/owner/bookings")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<List<OwnerBookingResponse>> listForOwner(Authentication authentication) {
        return ResponseEntity.ok(bookingService.listForOwner(authentication.getName()));
    }
}
