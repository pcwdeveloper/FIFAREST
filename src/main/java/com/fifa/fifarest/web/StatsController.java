package com.fifa.fifarest.web;

import com.fifa.fifarest.dto.AdminStatsResponse;
import com.fifa.fifarest.dto.OwnerStatsResponse;
import com.fifa.fifarest.service.AdminStatsService;
import com.fifa.fifarest.service.OwnerStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final AdminStatsService adminStatsService;
    private final OwnerStatsService ownerStatsService;

    public StatsController(AdminStatsService adminStatsService, OwnerStatsService ownerStatsService) {
        this.adminStatsService = adminStatsService;
        this.ownerStatsService = ownerStatsService;
    }

    @GetMapping("/api/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatsResponse> adminStats() {
        return ResponseEntity.ok(adminStatsService.getStats());
    }

    @GetMapping("/api/owner/stats")
    @PreAuthorize("hasRole('VENUE_OWNER')")
    public ResponseEntity<OwnerStatsResponse> ownerStats(Authentication authentication) {
        return ResponseEntity.ok(ownerStatsService.getStats(authentication.getName()));
    }
}
