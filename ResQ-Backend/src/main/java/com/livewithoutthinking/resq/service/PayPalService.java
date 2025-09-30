package com.livewithoutthinking.resq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livewithoutthinking.resq.entity.Bill;
import com.livewithoutthinking.resq.entity.Partner;
import com.livewithoutthinking.resq.entity.Payment;
import com.livewithoutthinking.resq.entity.RequestRescue;
import com.livewithoutthinking.resq.repository.BillRepository;
import com.livewithoutthinking.resq.repository.PartnerRepository;
import com.livewithoutthinking.resq.repository.PaymentRepository;
import com.livewithoutthinking.resq.repository.RequestRescueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PayPalService {

    private final BillRepository billRepository;
    private final RequestRescueRepository requestRescueRepository;
    private final PartnerRepository partnerRepository;
    private final PaymentRepository paymentRepository;
//    @Value("${paypal.client.id}")
//    private String liveId;
//    @Value("${paypal.client.secret}")
//    private String liveSecret;
//    @Value("${paypal.api.base-url}")
//    private String liveUrl;
    @Value("${sandbox.client.id}")
    private String sandboxId;
    @Value("${sandbox.client.secret}")
    private String sandboxSecret;
    @Value("${sandbox.api.base-url}")
    private String sandboxUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PayPalService(BillRepository billRepository, RequestRescueRepository requestRescueRepository, PartnerRepository partnerRepository, PaymentRepository paymentRepository) {
        this.billRepository = billRepository;
        this.requestRescueRepository = requestRescueRepository;
        this.partnerRepository = partnerRepository;
        this.paymentRepository = paymentRepository;
    }

    // Get Access Token
    public String getAccessToken(String id, String secret, String baseUrl) throws Exception {
        String credentials = id + ":" + secret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(id, secret);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/v1/oauth2/token", request, String.class);

        JsonNode json = objectMapper.readTree(response.getBody());
        return json.get("access_token").asText();
    }

    // Create Order PayPal
    public Map<String, String> createPayment(int rrId) throws Exception {
        String token = getAccessToken(sandboxId, sandboxSecret, sandboxUrl);

        RequestRescue rr = requestRescueRepository.findById(rrId)
                .orElseThrow(() -> new RuntimeException("Rescue Request not found"));
        Bill bill = billRepository.findBillsByReqResQ(rr.getRrid());
        double totalAmount = bill.getTotal();
        String currency = bill.getCurrency();
        String usdAmount;

        if ("USD".equalsIgnoreCase(currency)) {
            usdAmount = String.format("%.2f", totalAmount);
        } else {
            double exchangeRate = 26160.0;
            usdAmount = String.format("%.2f", totalAmount / exchangeRate);
        }



        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", new Object[]{
                        Map.of("amount", Map.of(
                                "currency_code", "USD",
                                "value", usdAmount
                        ))
                },
                "application_context", Map.of(
                        "return_url", "https://phuonglam208.github.io/ResQ_Paypal/payment-success.html", // hoặc để tạm
                        "cancel_url", "https://phuonglam208.github.io/ResQ_Paypal/payment-cancel.html"
                )
        );

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                sandboxUrl + "/v2/checkout/orders", HttpMethod.POST, entity, String.class);

        JsonNode json = objectMapper.readTree(response.getBody());
        String orderId = json.get("id").asText();

        String approveUrl = "";
        for (JsonNode link : json.get("links")) {
            if ("approve".equals(link.get("rel").asText())) {
                approveUrl = link.get("href").asText();
                break;
            }
        }

        return Map.of(
                "orderId", orderId,
                "approveUrl", approveUrl
        );
    }

    // Capture After User Approve
    public JsonNode capturePayment(String orderId) throws Exception {
        String token = getAccessToken(sandboxId, sandboxSecret, sandboxUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<String> response = restTemplate.exchange(
                sandboxUrl + "/v2/checkout/orders/" + orderId + "/capture",
                HttpMethod.POST,
                entity,
                String.class
        );
        return objectMapper.readTree(response.getBody());
    }

    // Paid For Partner
    public JsonNode sendPaymentToPartner(int partnerId) throws Exception {
        String token = getAccessToken(sandboxId, sandboxSecret, sandboxUrl);
        Partner partner = partnerRepository.findPartnerById(partnerId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Payment payment = paymentRepository.partnerPaypalPayment(partner.getUser().getUserId());
        double exchangeRate = 26160.0;
        double walletAmount = Double.parseDouble(String.valueOf(partner.getWalletAmount()));
        double adjustedAmount = (walletAmount - 5000) / exchangeRate;
        String amount = String.format("%.2f", adjustedAmount);

        Map<String, Object> payoutItem = Map.of(
                "recipient_type", "EMAIL",
                "amount", Map.of("value", amount, "currency", "USD"),
                "receiver", payment.getPaypalEmail(),
                "note", "Paid from ResQ",
                "sender_item_id", UUID.randomUUID().toString()
        );
        Map<String, Object> body = Map.of(
                "sender_batch_header", Map.of(
                        "sender_batch_id", UUID.randomUUID().toString(),
                        "email_subject", "You recieved payment from RESQ"
                ),
                "items", List.of(payoutItem)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                sandboxUrl +"/v1/payments/payouts", request, JsonNode.class);

        return response.getBody();
    }

    // Get Payout Status
    public String getPayoutStatus(String payoutBatchId) throws Exception {
        String token = getAccessToken(sandboxId, sandboxSecret, sandboxUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                sandboxUrl + "/v1/payments/payouts/" + payoutBatchId,
                HttpMethod.GET,
                entity,
                String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());

        // Truy cập "batch_header" → "batch_status"
        return root.path("batch_header").path("batch_status").asText();
    }


    public String refundToCustomer(int customerId, BigDecimal amount, String reason) throws Exception {
        String token = getAccessToken(sandboxId, sandboxSecret, sandboxUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Payment payment = paymentRepository.partnerPaypalPayment(customerId);
        double exchangeRate = 26160.0;
        double requestAmount = Double.parseDouble(String.valueOf(amount));
        double adjustedAmount = requestAmount / exchangeRate;
        String refundAmount = String.format("%.2f", adjustedAmount);

        Map<String, Object> payoutItem = Map.of(
                "recipient_type", "EMAIL",
                "amount", Map.of("value", refundAmount, "currency", "USD"),
                "receiver", payment.getPaypalEmail(),
                "note", reason,
                "sender_item_id", UUID.randomUUID().toString()
        );
        Map<String, Object> body = Map.of(
                "sender_batch_header", Map.of(
                        "sender_batch_id", UUID.randomUUID().toString(),
                        "email_subject", "You received refund from RESQ"
                ),
                "items", List.of(payoutItem)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                sandboxUrl + "/v1/payments/payouts", request, JsonNode.class
        );

        // Trích xuất payout_batch_id từ response JSON
        JsonNode responseBody = response.getBody();
        if (responseBody != null && responseBody.has("batch_header")) {
            return responseBody.path("batch_header").path("payout_batch_id").asText();
        }

        throw new RuntimeException("Failed to retrieve payout_batch_id from PayPal response.");
    }


}
