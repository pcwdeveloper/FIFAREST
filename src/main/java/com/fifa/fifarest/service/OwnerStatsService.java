package com.fifa.fifarest.service;

import com.fifa.fifarest.domain.BookingStatus;
import com.fifa.fifarest.domain.User;
import com.fifa.fifarest.domain.VenueStatus;
import com.fifa.fifarest.dto.OwnerStatsResponse;
import com.fifa.fifarest.repository.BookingRepository;
import com.fifa.fifarest.repository.CourtRepository;
import com.fifa.fifarest.repository.VenueRepository;
import org.springframework.stereotype.Service;

@Service
public class OwnerStatsService {

    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    public OwnerStatsService(VenueRepository venueRepository, CourtRepository courtRepository,
                              BookingRepository bookingRepository, UserService userService) {
        this.venueRepository = venueRepository;
        this.courtRepository = courtRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
    }

    public OwnerStatsResponse getStats(String ownerEmail) {
        User owner = userService.getByEmail(ownerEmail);

        return new OwnerStatsResponse(
                venueRepository.countByOwner(owner),
                venueRepository.countByOwnerAndStatus(owner, VenueStatus.APPROVED),
                venueRepository.countByOwnerAndStatus(owner, VenueStatus.PENDING),
                courtRepository.countByVenue_Owner(owner),
                bookingRepository.countBySlot_Court_Venue_OwnerAndStatus(owner, BookingStatus.CONFIRMED),
                bookingRepository.countBySlot_Court_Venue_OwnerAndStatus(owner, BookingStatus.PENDING_PAYMENT),
                bookingRepository.sumAmountByOwnerAndStatus(owner, BookingStatus.CONFIRMED)
        );
    }
}
