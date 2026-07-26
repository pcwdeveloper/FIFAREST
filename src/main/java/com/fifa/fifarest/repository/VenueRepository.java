package com.fifa.fifarest.repository;

import com.fifa.fifarest.domain.User;
import com.fifa.fifarest.domain.Venue;
import com.fifa.fifarest.domain.VenueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByOwner(User owner);

    List<Venue> findByStatus(VenueStatus status);

    long countByStatus(VenueStatus status);

    long countByOwner(User owner);

    long countByOwnerAndStatus(User owner, VenueStatus status);
}
