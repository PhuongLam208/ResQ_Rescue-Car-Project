package com.livewithoutthinking.resq.controller;

import com.livewithoutthinking.resq.service.PayPalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/paypal/webhook")
public class PayPalWebhookController {

    private final PayPalService payPalService;

    public PayPalWebhookController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        String eventType = (String) payload.get("event_type");

        if ("CHECKOUT.ORDER.APPROVED".equals(eventType)) {
            Map<String, Object> resource = (Map<String, Object>) payload.get("resource");
            String orderId = (String) resource.get("id");

            try {
                payPalService.capturePayment(orderId);
                return ResponseEntity.ok("Captured");
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Capture failed: " + e.getMessage());
            }
        }

        return ResponseEntity.ok("Event ignored");
    }
}
