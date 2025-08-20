package fsa.training.travelee.controller.admin;

import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class BookingAdminController {

    private final BookingService bookingService;

    @GetMapping
    public String listBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        
        Page<Booking> bookings;
        
        if (status != null && !status.isEmpty()) {
            try {
                BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                bookings = bookingService.getBookingsByStatusWithPagination(bookingStatus, pageable);
            } catch (IllegalArgumentException e) {
                bookings = bookingService.getAllBookingsWithPagination(pageable);
            }
        } else if (search != null && !search.trim().isEmpty()) {
            bookings = bookingService.searchBookings(search, pageable);
        } else if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00", formatter);
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59", formatter);
            bookings = bookingService.getBookingsByDateRange(start, end, pageable);
        } else {
            Pageable sortedPageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            bookings = bookingService.getAllBookingsWithPagination(sortedPageable);
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookings.getTotalPages());
        model.addAttribute("totalItems", bookings.getTotalElements());
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchTerm", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/booking/booking-list";
    }

    @GetMapping("/{id}")
    public String viewBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingService.getBookingById(id);
        if (booking == null) {
            return "redirect:/admin/bookings";
        }
        
        model.addAttribute("booking", booking);
        // Trích lý do hủy (nếu có) từ specialRequests để hiển thị riêng
        String cancellationReason = null;
        String special = booking.getSpecialRequests();
        if (special != null) {
            int idxAdmin = special.indexOf("Admin hủy:");
            int idxUser = special.indexOf("Lý do hủy:");
            if (idxAdmin >= 0) {
                cancellationReason = special.substring(idxAdmin + "Admin hủy:".length()).trim();
            } else if (idxUser >= 0) {
                cancellationReason = special.substring(idxUser + "Lý do hủy:".length()).trim();
            }
        }
        model.addAttribute("cancellationReason", cancellationReason);
        return "admin/booking/booking-detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (status == BookingStatus.CANCELLED && reason != null) {
                // Admin hủy (cho phép cả khi đã thanh toán)
                bookingService.cancelBookingByAdmin(id, reason);
            } else {
                // Các trạng thái khác thì gọi updateBookingStatus
                bookingService.updateBookingStatus(id, status);
            }
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái booking thành công!");
            return "redirect:/admin/bookings/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/bookings/" + id;
        }
    }

    @PostMapping("/{id}/refund")
    public String refundBooking(@PathVariable Long id,
                                @RequestParam("amount") java.math.BigDecimal amount,
                                @RequestParam("reason") String reason,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.refundBooking(id, amount, reason);
            redirectAttributes.addFlashAttribute("success", "Hoàn tiền thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hoàn tiền: " + e.getMessage());
        }
        return "redirect:/admin/bookings/" + id;
    }

    @GetMapping("/statistics")
    public String getStatistics(Model model) {
        long totalBookings = bookingService.getTotalBookings();
        long pendingBookings = bookingService.getTotalBookingsByStatus(BookingStatus.PENDING);
        long confirmedBookings = bookingService.getTotalBookingsByStatus(BookingStatus.CONFIRMED);
        long cancelledBookings = bookingService.getTotalBookingsByStatus(BookingStatus.CANCELLED);
        long completedBookings = bookingService.getTotalBookingsByStatus(BookingStatus.COMPLETED);
        
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("completedBookings", completedBookings);
        
        return "admin/booking/booking-statistics";
    }
}
