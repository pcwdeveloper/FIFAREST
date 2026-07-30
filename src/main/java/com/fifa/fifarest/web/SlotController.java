package com.fifa.fifarest.web;

import com.fifa.fifarest.dto.BulkSlotCreateResponse;
import com.fifa.fifarest.dto.BulkSlotRequest;
import com.fifa.fifarest.dto.SlotRequest;
import com.fifa.fifarest.dto.SlotResponse;
import com.fifa.fifarest.dto.SlotStatusUpdateRequest;
import com.fifa.fifarest.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping("/api/courts/{courtId}/slots")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<SlotResponse> create(Authentication authentication, @PathVariable Long courtId,
                                                @Valid @RequestBody SlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(slotService.create(courtId, authentication.getName(), request));
    }

    @PostMapping("/api/courts/{courtId}/slots/bulk")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<BulkSlotCreateResponse> generateForMonth(Authentication authentication, @PathVariable Long courtId,
                                                                    @Valid @RequestBody BulkSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(slotService.generateForMonth(courtId, authentication.getName(), request));
    }

    @GetMapping("/api/courts/{courtId}/slots")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<List<SlotResponse>> listByCourt(Authentication authentication, @PathVariable Long courtId) {
        return ResponseEntity.ok(slotService.listByCourtForOwner(courtId, authentication.getName()));
    }

    @GetMapping("/api/courts/{courtId}/slots/public")
    public ResponseEntity<List<SlotResponse>> listPublicByCourtAndDate(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.listAvailableForPublic(courtId, date));
    }

    @PatchMapping("/api/slots/{id}/status")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<SlotResponse> updateStatus(Authentication authentication, @PathVariable Long id,
                                                       @Valid @RequestBody SlotStatusUpdateRequest request) {
        return ResponseEntity.ok(slotService.updateStatus(id, authentication.getName(), request));
    }

    @DeleteMapping("/api/slots/{id}")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        slotService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
