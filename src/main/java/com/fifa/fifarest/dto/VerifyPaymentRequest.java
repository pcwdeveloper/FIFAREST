package com.fifa.fifarest.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyPaymentRequest(
        @NotBlank(message = "Razorpay order id is required")
        String razorpayOrderId,

        @NotBlank(message = "Razorpay payment id is required")
        String razorpayPaymentId,

        @NotBlank(message = "Razorpay signature is required")
        String razorpaySignature
) {
}
