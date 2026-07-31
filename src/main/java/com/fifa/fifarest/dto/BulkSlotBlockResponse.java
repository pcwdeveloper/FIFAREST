package com.fifa.fifarest.dto;

public record BulkSlotBlockResponse(
        int blockedCount,
        int skippedCount
) {
}
