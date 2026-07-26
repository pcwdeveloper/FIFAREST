package com.fifa.fifarest.dto;

import java.math.BigDecimal;

public record BookingInitResponse(
        BookingResponse booking,
        String razorpayOrderId,
        String razorpayKeyId,
        BigDecimal amount,
        String currency
) {
}
