package fsa.training.travelee.controller.webhook;

import fsa.training.travelee.dto.payment.SepayWebhookDto;
import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/webhook/sepay")
@RequiredArgsConstructor
@Slf4j
public class SepayWebhookController {

    private final PaymentService paymentService;

    @Value("${sepay.webhook.api-key}")
    private String sepayWebhookApiKey;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody SepayWebhookDto payload,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        if (!isAuthorized(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid webhook credential");
        }

        log.info("Nhận webhook SePay: ref={}, status={}, amount={}",
                payload.getReferenceCode(), payload.getStatus(), payload.getAmount());

        Booking booking = paymentService.handleSepayWebhook(payload);
        return ResponseEntity.ok("OK:" + booking.getBookingCode());
    }

    @PostMapping("/{bookingCode}")
    public ResponseEntity<String> handleWebhookWithBookingCode(
            @PathVariable String bookingCode,
            @RequestBody SepayWebhookDto payload,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        if (!isAuthorized(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid webhook credential");
        }

        // Ưu tiên bookingCode từ path
        payload.setReferenceCode(bookingCode);
        log.info("Nhận webhook SePay theo path: bookingCode={}, status={}, amount={}",
                bookingCode, payload.getStatus(), payload.getAmount());

        Booking booking = paymentService.handleSepayWebhook(payload);
        return ResponseEntity.ok("OK:" + booking.getBookingCode());
    }

    private boolean isAuthorized(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Apikey ")) {
            log.warn("Thiếu hoặc sai định dạng Authorization header");
            return false;
        }
        String token = authorizationHeader.substring("Apikey ".length());
        return token.equals(sepayWebhookApiKey);
    }
}
