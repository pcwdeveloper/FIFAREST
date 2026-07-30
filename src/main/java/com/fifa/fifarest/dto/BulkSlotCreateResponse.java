package com.fifa.fifarest.dto;

import java.util.List;

public record BulkSlotCreateResponse(
        List<SlotResponse> created,
        int skippedCount
) {
}
