package com.fifa.fifarest.service;

import com.fifa.fifarest.domain.Court;
import com.fifa.fifarest.domain.Slot;
import com.fifa.fifarest.domain.SlotStatus;
import com.fifa.fifarest.dto.SlotRequest;
import com.fifa.fifarest.dto.SlotResponse;
import com.fifa.fifarest.dto.SlotStatusUpdateRequest;
import com.fifa.fifarest.exception.ForbiddenActionException;
import com.fifa.fifarest.exception.ResourceNotFoundException;
import com.fifa.fifarest.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final CourtService courtService;

    public SlotService(SlotRepository slotRepository, CourtService courtService) {
        this.slotRepository = slotRepository;
        this.courtService = courtService;
    }

    @Transactional
    public SlotResponse create(Long courtId, String requesterEmail, SlotRequest request) {
        Court court = courtService.getByIdForOwner(courtId, requesterEmail);

        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        boolean overlaps = slotRepository.findByCourtAndDate(court, request.date()).stream()
                .anyMatch(existing -> request.startTime().isBefore(existing.getEndTime())
                        && request.endTime().isAfter(existing.getStartTime()));
        if (overlaps) {
            throw new IllegalArgumentException("This slot overlaps with an existing slot for this court");
        }

        Slot slot = Slot.builder()
                .court(court)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(SlotStatus.AVAILABLE)
                .build();

        return SlotResponse.from(slotRepository.save(slot));
    }

    public List<SlotResponse> listByCourtForOwner(Long courtId, String requesterEmail) {
        Court court = courtService.getByIdForOwner(courtId, requesterEmail);
        return slotRepository.findByCourtOrderByDateAscStartTimeAsc(court).stream()
                .map(SlotResponse::from)
                .toList();
    }

    public List<SlotResponse> listAvailableForPublic(Long courtId, LocalDate date) {
        Court court = courtService.getByIdForPublicBrowse(courtId);
        return slotRepository.findByCourtAndDateAndStatusOrderByStartTimeAsc(court, date, SlotStatus.AVAILABLE).stream()
                .map(SlotResponse::from)
                .toList();
    }

    @Transactional
    public SlotResponse updateStatus(Long slotId, String requesterEmail, SlotStatusUpdateRequest request) {
        if (request.status() == SlotStatus.BOOKED) {
            throw new IllegalArgumentException("Slot status can only be set to AVAILABLE or BLOCKED here");
        }

        Slot slot = getById(slotId);
        assertOwner(slot, requesterEmail);

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new IllegalArgumentException("A booked slot cannot be changed");
        }

        slot.setStatus(request.status());
        return SlotResponse.from(slotRepository.save(slot));
    }

    @Transactional
    public void delete(Long slotId, String requesterEmail) {
        Slot slot = getById(slotId);
        assertOwner(slot, requesterEmail);

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new IllegalArgumentException("A booked slot cannot be deleted");
        }

        slotRepository.delete(slot);
    }

    private Slot getById(Long id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));
    }

    private void assertOwner(Slot slot, String requesterEmail) {
        if (!slot.getCourt().getVenue().getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new ForbiddenActionException("You do not own this slot");
        }
    }
}
