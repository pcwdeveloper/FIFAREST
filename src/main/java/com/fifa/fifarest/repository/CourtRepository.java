package com.fifa.fifarest.repository;

import com.fifa.fifarest.domain.Court;
import com.fifa.fifarest.domain.Sport;
import com.fifa.fifarest.domain.User;
import com.fifa.fifarest.domain.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByVenue(Venue venue);

    List<Court> findByVenueAndSport(Venue venue, Sport sport);

    long countByVenue_Owner(User owner);
}
