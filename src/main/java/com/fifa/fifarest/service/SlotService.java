package com.fifa.fifarest.service;

import com.fifa.fifarest.domain.Court;
import com.fifa.fifarest.domain.Slot;
import com.fifa.fifarest.domain.SlotStatus;
import com.fifa.fifarest.domain.TimeOfDay;
import com.fifa.fifarest.dto.BulkSlotBlockRequest;
import com.fifa.fifarest.dto.BulkSlotBlockResponse;
import com.fifa.fifarest.dto.BulkSlotCreateResponse;
import com.fifa.fifarest.dto.BulkSlotDeleteRequest;
import com.fifa.fifarest.dto.BulkSlotDeleteResponse;
import com.fifa.fifarest.dto.BulkSlotRequest;
import com.fifa.fifarest.dto.SlotResponse;
import com.fifa.fifarest.dto.SlotStatusUpdateRequest;
import com.fifa.fifarest.exception.ForbiddenActionException;
import com.fifa.fifarest.exception.ResourceNotFoundException;
import com.fifa.fifarest.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final CourtService courtService;

    public SlotService(SlotRepository slotRepository, CourtService courtService) {
        this.slotRepository = slotRepository;
        this.courtService = courtService;
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
    public BulkSlotCreateResponse generateForMonth(Long courtId, String requesterEmail, BulkSlotRequest request) {
        Court court = courtService.getByIdForOwner(courtId, requesterEmail);

        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(request.year(), request.month());
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid year/month");
        }
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        if (request.intervalMinutes() == null || request.intervalMinutes() < 15) {
            throw new IllegalArgumentException("Interval must be at least 15 minutes");
        }

        List<CategoryPlan> plans = new ArrayList<>();
        addPlanIfEnabled(plans, TimeOfDay.MORNING, request.morning(), request.intervalMinutes());
        addPlanIfEnabled(plans, TimeOfDay.AFTERNOON, request.afternoon(), request.intervalMinutes());
        addPlanIfEnabled(plans, TimeOfDay.EVENING, request.evening(), request.intervalMinutes());
        addPlanIfEnabled(plans, TimeOfDay.NIGHT, request.night(), request.intervalMinutes());

        if (plans.isEmpty()) {
            throw new IllegalArgumentException("Select at least one time-of-day category to generate slots for");
        }

        plans.sort(Comparator.comparingInt(CategoryPlan::startMinutes));
        for (int i = 1; i < plans.size(); i++) {
            CategoryPlan previous = plans.get(i - 1);
            CategoryPlan current = plans.get(i);
            if (current.startMinutes() < previous.endMinutes()) {
                throw new IllegalArgumentException(previous.timeOfDay() + " and " + current.timeOfDay()
                        + " time ranges overlap — each category's time range must start after the previous one ends");
            }
        }

        Map<LocalDate, List<Slot>> existingByDate = slotRepository.findByCourtAndDateBetween(court, monthStart, monthEnd)
                .stream()
                .collect(Collectors.groupingBy(Slot::getDate));

        List<Slot> toCreate = new ArrayList<>();
        int skipped = 0;

        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            List<Slot> existingForDate = existingByDate.computeIfAbsent(date, d -> new ArrayList<>());

            for (CategoryPlan plan : plans) {
                for (int[] window : generateWindows(plan.startMinutes(), plan.endMinutes(), plan.intervalMinutes())) {
                    LocalTime start = LocalTime.of(window[0] / 60, window[0] % 60);
                    LocalTime end = LocalTime.of(window[1] / 60, window[1] % 60);

                    boolean overlaps = existingForDate.stream()
                            .anyMatch(existing -> start.isBefore(existing.getEndTime())
                                    && end.isAfter(existing.getStartTime()));
                    if (overlaps) {
                        skipped++;
                        continue;
                    }

                    Slot slot = Slot.builder()
                            .court(court)
                            .date(date)
                            .startTime(start)
                            .endTime(end)
                            .price(plan.price())
                            .category(plan.timeOfDay())
                            .status(SlotStatus.AVAILABLE)
                            .build();
                    toCreate.add(slot);
                    existingForDate.add(slot);
                }
            }
        }

        List<SlotResponse> created = slotRepository.saveAll(toCreate).stream().map(SlotResponse::from).toList();
        return new BulkSlotCreateResponse(created, skipped);
    }

    private void addPlanIfEnabled(List<CategoryPlan> plans, TimeOfDay timeOfDay, BulkSlotRequest.CategoryConfig config,
                                   int intervalMinutes) {
        if (config == null || !config.enabled()) {
            return;
        }
        if (config.startTime() == null || config.endTime() == null) {
            throw new IllegalArgumentException(timeOfDay + " start time and end time are required");
        }
        if (!config.endTime().isAfter(config.startTime())) {
            throw new IllegalArgumentException(timeOfDay + " end time must be after start time");
        }
        if (config.price() == null || config.price().signum() < 0) {
            throw new IllegalArgumentException(timeOfDay + " price must not be negative");
        }
        int startMinutes = config.startTime().getHour() * 60 + config.startTime().getMinute();
        int endMinutes = config.endTime().getHour() * 60 + config.endTime().getMinute();
        plans.add(new CategoryPlan(timeOfDay, startMinutes, endMinutes, intervalMinutes, config.price()));
    }

    private List<int[]> generateWindows(int windowStartMinutes, int windowEndMinutes, int intervalMinutes) {
        List<int[]> windows = new ArrayList<>();
        for (int start = windowStartMinutes; start + intervalMinutes <= windowEndMinutes; start += intervalMinutes) {
            windows.add(new int[]{start, start + intervalMinutes});
        }
        return windows;
    }

    private record CategoryPlan(TimeOfDay timeOfDay, int startMinutes, int endMinutes, int intervalMinutes, BigDecimal price) {
    }

    @Transactional
    public BulkSlotDeleteResponse deleteForMonth(Long courtId, String requesterEmail, BulkSlotDeleteRequest request) {
        Court court = courtService.getByIdForOwner(courtId, requesterEmail);

        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(request.year(), request.month());
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid year/month");
        }

        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<Slot> candidates = slotRepository.findByCourtAndDateBetween(court, monthStart, monthEnd).stream()
                .filter(slot -> request.categories().contains(resolveCategory(slot)))
                .toList();

        List<Slot> deletable = candidates.stream().filter(slot -> slot.getStatus() != SlotStatus.BOOKED).toList();
        int skipped = candidates.size() - deletable.size();

        slotRepository.deleteAll(deletable);
        return new BulkSlotDeleteResponse(deletable.size(), skipped);
    }

    private TimeOfDay resolveCategory(Slot slot) {
        return slot.getCategory() != null ? slot.getCategory() : TimeOfDay.fromStartTime(slot.getStartTime());
    }

    @Transactional
    public BulkSlotBlockResponse blockForDateRange(Long courtId, String requesterEmail, BulkSlotBlockRequest request) {
        Court court = courtService.getByIdForOwner(courtId, requesterEmail);

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("End date must not be before start date");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        List<Slot> candidates = slotRepository.findByCourtAndDateBetween(court, request.startDate(), request.endDate())
                .stream()
                .filter(slot -> !slot.getStartTime().isBefore(request.startTime())
                        && !slot.getEndTime().isAfter(request.endTime()))
                .toList();

        List<Slot> blockable = candidates.stream().filter(slot -> slot.getStatus() == SlotStatus.AVAILABLE).toList();
        int skipped = candidates.size() - blockable.size();

        blockable.forEach(slot -> slot.setStatus(SlotStatus.BLOCKED));
        slotRepository.saveAll(blockable);

        return new BulkSlotBlockResponse(blockable.size(), skipped);
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
