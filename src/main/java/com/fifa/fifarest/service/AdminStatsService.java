package com.fifa.fifarest.service;

import com.fifa.fifarest.domain.BookingStatus;
import com.fifa.fifarest.domain.Role;
import com.fifa.fifarest.domain.VenueStatus;
import com.fifa.fifarest.dto.AdminStatsResponse;
import com.fifa.fifarest.repository.BookingRepository;
import com.fifa.fifarest.repository.SportRepository;
import com.fifa.fifarest.repository.UserRepository;
import com.fifa.fifarest.repository.VenueRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminStatsService {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final SportRepository sportRepository;
    private final BookingRepository bookingRepository;

    public AdminStatsService(UserRepository userRepository, VenueRepository venueRepository,
                              SportRepository sportRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
        this.sportRepository = sportRepository;
        this.bookingRepository = bookingRepository;
    }

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.countByRole(Role.PLAYER),
                userRepository.countByRole(Role.VENUE_OWNER),
                venueRepository.countByStatus(VenueStatus.PENDING),
                venueRepository.countByStatus(VenueStatus.APPROVED),
                venueRepository.countByStatus(VenueStatus.REJECTED),
                sportRepository.count(),
                bookingRepository.countByStatus(BookingStatus.CONFIRMED),
                bookingRepository.countByStatus(BookingStatus.CANCELLED),
                bookingRepository.sumAmountByStatus(BookingStatus.CONFIRMED)
        );
    }
}
