package com.livewithoutthinking.resq.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.livewithoutthinking.resq.service.PayPalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
public class PayPalController {

    private final PayPalService payPalService;

    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    @PostMapping("/create/{rrId}")
    public ResponseEntity<Map<String, String>> create(@PathVariable int rrId) throws Exception {
        return ResponseEntity.ok(payPalService.createPayment(rrId));
    }

    @PostMapping("/capture/{orderId}")
    public ResponseEntity<JsonNode> capture(@PathVariable String orderId) throws Exception {
        return ResponseEntity.ok(payPalService.capturePayment(orderId));
    }

    //Partner Paument
    @PostMapping("/payPartner/{partnerId}")
    public ResponseEntity<?> payToPartner(@PathVariable int partnerId) {
        try {
            JsonNode result = payPalService.sendPaymentToPartner(partnerId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/checkPayoutStatus/{batchId}")
    public ResponseEntity<?> checkPayoutStatus(@PathVariable String batchId) {
        try {
            String result = payPalService.getPayoutStatus(batchId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
