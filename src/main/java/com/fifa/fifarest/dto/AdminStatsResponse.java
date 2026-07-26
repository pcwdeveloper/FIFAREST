package com.fifa.fifarest.dto;

import java.math.BigDecimal;

public record AdminStatsResponse(
        long totalPlayers,
        long totalOwners,
        long pendingVenues,
        long approvedVenues,
        long rejectedVenues,
        long totalSports,
        long confirmedBookings,
        long cancelledBookings,
        BigDecimal totalRevenue
) {
}
