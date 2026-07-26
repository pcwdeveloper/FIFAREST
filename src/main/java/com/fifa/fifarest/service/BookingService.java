package com.fifa.fifarest.service;

import com.fifa.fifarest.domain.*;
import com.fifa.fifarest.dto.BookingInitResponse;
import com.fifa.fifarest.dto.BookingResponse;
import com.fifa.fifarest.dto.CreateBookingRequest;
import com.fifa.fifarest.dto.OwnerBookingResponse;
import com.fifa.fifarest.dto.VerifyPaymentRequest;
import com.fifa.fifarest.exception.ForbiddenActionException;
import com.fifa.fifarest.exception.ResourceNotFoundException;
import com.fifa.fifarest.exception.SlotUnavailableException;
import com.fifa.fifarest.repository.BookingRepository;
import com.fifa.fifarest.repository.PaymentRepository;
import com.fifa.fifarest.repository.SlotRepository;
import com.razorpay.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingService {

    private static final String CURRENCY = "INR";

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SlotRepository slotRepository;
    private final UserService userService;
    private final RazorpayClientService razorpayClientService;

    public BookingService(BookingRepository bookingRepository, PaymentRepository paymentRepository,
                           SlotRepository slotRepository, UserService userService,
                           RazorpayClientService razorpayClientService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.slotRepository = slotRepository;
        this.userService = userService;
        this.razorpayClientService = razorpayClientService;
    }

    @Transactional
    public BookingInitResponse createBooking(String playerEmail, CreateBookingRequest request) {
        Slot slot = slotRepository.findById(request.slotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + request.slotId()));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotUnavailableException("This slot is no longer available");
        }

        User player = userService.getByEmail(playerEmail);
        BigDecimal amount = slot.getCourt().getPricePerSlot();

        // Reserve the slot immediately so a second player can't book it while payment is pending.
        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        Booking booking = Booking.builder()
                .slot(slot)
                .player(player)
                .amount(amount)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();
        booking = bookingRepository.save(booking);

        Order order = razorpayClientService.createOrder(amount, CURRENCY, "booking-" + booking.getId());
        String razorpayOrderId = order.get("id");

        Payment payment = Payment.builder()
                .booking(booking)
                .razorpayOrderId(razorpayOrderId)
                .amount(amount)
                .currency(CURRENCY)
                .status(PaymentStatus.CREATED)
                .build();
        paymentRepository.save(payment);

        return new BookingInitResponse(
                BookingResponse.from(booking), razorpayOrderId, razorpayClientService.getKeyId(), amount, CURRENCY);
    }

    @Transactional
    public BookingResponse verifyPayment(Long bookingId, String playerEmail, VerifyPaymentRequest request) {
        Booking booking = getById(bookingId);
        assertOwner(booking, playerEmail);

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("This booking is not awaiting payment");
        }

        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking: " + bookingId));

        boolean valid = razorpayClientService.verifySignature(
                request.razorpayOrderId(), request.razorpayPaymentId(), request.razorpaySignature());

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new IllegalArgumentException("Payment verification failed");
        }

        payment.setRazorpayPaymentId(request.razorpayPaymentId());
        payment.setRazorpaySignature(request.razorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

        return BookingResponse.from(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId, String playerEmail) {
        Booking booking = getById(bookingId);
        assertOwner(booking, playerEmail);

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Only a booking awaiting payment can be cancelled this way");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        Slot slot = booking.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);
    }

    public List<BookingResponse> listMine(String playerEmail) {
        User player = userService.getByEmail(playerEmail);
        return bookingRepository.findByPlayerOrderByCreatedAtDesc(player).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public List<OwnerBookingResponse> listForOwner(String ownerEmail) {
        User owner = userService.getByEmail(ownerEmail);
        return bookingRepository.findBySlot_Court_Venue_OwnerOrderByCreatedAtDesc(owner).stream()
                .map(OwnerBookingResponse::from)
                .toList();
    }

    private Booking getById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    private void assertOwner(Booking booking, String playerEmail) {
        if (!booking.getPlayer().getEmail().equalsIgnoreCase(playerEmail)) {
            throw new ForbiddenActionException("This booking does not belong to you");
        }
    }
}
