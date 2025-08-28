package fsa.training.travelee.controller;

import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller demo để minh họa cách sử dụng hệ thống cập nhật số chỗ còn lại
 * Chỉ sử dụng cho mục đích demo và testing
 */
@RestController
@RequestMapping("/api/demo/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingDemoController {

    private final BookingService bookingService;

    /**
     * Demo: Xác nhận booking (PENDING -> CONFIRMED)
     * Số chỗ sẽ bị trừ
     */
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmBooking(@PathVariable Long bookingId) {
        try {
            var booking = bookingService.updateBookingStatus(bookingId, BookingStatus.CONFIRMED);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xác nhận booking thành công");
            response.put("bookingId", booking.getId());
            response.put("newStatus", booking.getStatus());
            response.put("availableSlots", booking.getSchedule().getAvailableSlots());
            
            log.info("Demo: Đã xác nhận booking {} - Số chỗ còn lại: {}", 
                bookingId, booking.getSchedule().getAvailableSlots());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Demo: Lỗi khi xác nhận booking {}: {}", bookingId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Demo: Thanh toán booking (PENDING/CONFIRMED -> PAID)
     * Số chỗ sẽ bị trừ nếu từ PENDING, không thay đổi nếu từ CONFIRMED
     */
    @PostMapping("/{bookingId}/pay")
    public ResponseEntity<Map<String, Object>> payBooking(@PathVariable Long bookingId) {
        try {
            var booking = bookingService.updateBookingStatus(bookingId, BookingStatus.PAID);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã thanh toán booking thành công");
            response.put("bookingId", booking.getId());
            response.put("newStatus", booking.getStatus());
            response.put("availableSlots", booking.getSchedule().getAvailableSlots());
            
            log.info("Demo: Đã thanh toán booking {} - Số chỗ còn lại: {}", 
                bookingId, booking.getSchedule().getAvailableSlots());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Demo: Lỗi khi thanh toán booking {}: {}", bookingId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Demo: Hủy booking (bất kỳ trạng thái nào -> CANCELLED)
     * Số chỗ sẽ được hoàn tác nếu trước đó đã trừ
     */
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam String reason) {
        try {
            var booking = bookingService.cancelBooking(bookingId, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã hủy booking thành công");
            response.put("bookingId", booking.getId());
            response.put("newStatus", booking.getStatus());
            response.put("availableSlots", booking.getSchedule().getAvailableSlots());
            
            log.info("Demo: Đã hủy booking {} - Số chỗ còn lại: {}", 
                bookingId, booking.getSchedule().getAvailableSlots());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Demo: Lỗi khi hủy booking {}: {}", bookingId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Demo: Hoàn tiền booking (PAID -> REFUNDED)
     * Số chỗ sẽ được hoàn tác
     */
    @PostMapping("/{bookingId}/refund")
    public ResponseEntity<Map<String, Object>> refundBooking(
            @PathVariable Long bookingId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        try {
            var booking = bookingService.refundBooking(bookingId, amount, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã hoàn tiền booking thành công");
            response.put("bookingId", booking.getId());
            response.put("newStatus", booking.getStatus());
            response.put("availableSlots", booking.getSchedule().getAvailableSlots());
            response.put("refundAmount", amount);
            
            log.info("Demo: Đã hoàn tiền booking {} - Số chỗ còn lại: {}", 
                bookingId, booking.getSchedule().getAvailableSlots());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Demo: Lỗi khi hoàn tiền booking {}: {}", bookingId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Demo: Hoàn thành tour (PAID/CONFIRMED -> COMPLETED)
     * Số chỗ không thay đổi
     */
    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<Map<String, Object>> completeBooking(@PathVariable Long bookingId) {
        try {
            var booking = bookingService.updateBookingStatus(bookingId, BookingStatus.COMPLETED);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã hoàn thành tour thành công");
            response.put("bookingId", booking.getId());
            response.put("newStatus", booking.getStatus());
            response.put("availableSlots", booking.getSchedule().getAvailableSlots());
            
            log.info("Demo: Đã hoàn thành tour {} - Số chỗ còn lại: {}", 
                bookingId, booking.getSchedule().getAvailableSlots());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Demo: Lỗi khi hoàn thành tour {}: {}", bookingId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Demo: Khôi phục booking đã hủy (CANCELLED -> CONFIRMED)
     * Số chỗ sẽ bị trừ lại
     */
    @PostMapping("/{bookingId}/restore")
    public ResponseEntity<Map<String, Object>> restoreBooking(@PathVariable Long bookingId) {
        try {
            var booking = bookingService.updateBookingStatus(bookingId, BookingStatus.CONFIRMED);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã khôi phục booking thành công");
            response.put("bookingId", booking.getId());
            response.put("newStatus", booking.getStatus());
            response.put("availableSlots", booking.getSchedule().getAvailableSlots());
            
            log.info("Demo: Đã khôi phục booking {} - Số chỗ còn lại: {}", 
                bookingId, booking.getSchedule().getAvailableSlots());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Demo: Lỗi khi khôi phục booking {}: {}", bookingId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
