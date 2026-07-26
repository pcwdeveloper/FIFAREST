package com.fifa.fifarest.service;

import com.fifa.fifarest.exception.PaymentGatewayException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RazorpayClientService {

    private final String keyId;
    private final String keySecret;

    public RazorpayClientService(
            @Value("${app.razorpay.key-id}") String keyId,
            @Value("${app.razorpay.key-secret}") String keySecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    public String getKeyId() {
        return keyId;
    }

    public Order createOrder(BigDecimal amount, String currency, String receipt) {
        assertConfigured();
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject request = new JSONObject();
            request.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            request.put("currency", currency);
            request.put("receipt", receipt);
            return client.orders.create(request);
        } catch (RazorpayException e) {
            throw new PaymentGatewayException("Failed to create Razorpay order", e);
        }
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        assertConfigured();
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (RazorpayException e) {
            return false;
        }
    }

    private void assertConfigured() {
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new PaymentGatewayException(
                    "Razorpay is not configured — set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET");
        }
    }
}
