package com.fifa.fifarest.dto;

import java.math.BigDecimal;

public record OwnerStatsResponse(
        long totalVenues,
        long approvedVenues,
        long pendingVenues,
        long totalCourts,
        long confirmedBookings,
        long pendingBookings,
        BigDecimal totalRevenue
) {
}
