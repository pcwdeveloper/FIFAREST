package com.fifa.fifarest.dto;

public record BulkSlotDeleteResponse(
        int deletedCount,
        int skippedCount
) {
}
