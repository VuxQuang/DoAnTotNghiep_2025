package fsa.training.travelee.controller.page;

import fsa.training.travelee.dto.BookingRequest;
import fsa.training.travelee.entity.Booking;
import fsa.training.travelee.entity.BookingStatus;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.service.BookingService;
import fsa.training.travelee.service.TourClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;

@Controller
@RequestMapping("/page/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final TourClientService tourClientService;

    // Hiển thị form đặt tour
    @GetMapping("/{tourId}")
    public String showBookingForm(@PathVariable Long tourId, 
                                 @RequestParam(required = false) Long scheduleId,
                                 Model model) {
        try {
            Tour tour = tourClientService.getTourById(tourId);
            if (tour == null) {
                return "redirect:/page/tours";
            }

            model.addAttribute("tour", tour);
            model.addAttribute("bookingRequest", new BookingRequest());
            model.addAttribute("scheduleId", scheduleId);
            
            return "page/booking/booking-form";
        } catch (Exception e) {
            return "redirect:/page/tours";
        }
    }

    // Xử lý đặt tour
    @PostMapping("/create")
    public String createBooking(@Valid @ModelAttribute BookingRequest bookingRequest,
                               BindingResult bindingResult,
                               @RequestParam Long userId,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            // Lấy lại thông tin tour để hiển thị form
            Tour tour = tourClientService.getTourById(bookingRequest.getTourId());
            model.addAttribute("tour", tour);
            model.addAttribute("scheduleId", bookingRequest.getScheduleId());
            return "page/booking/booking-form";
        }

        try {
            // Tạo booking
            Booking booking = bookingService.createBooking(bookingRequest, userId);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đặt tour thành công! Mã booking: " + booking.getId());
            redirectAttributes.addFlashAttribute("booking", booking);
            
            return "redirect:/page/booking/confirmation/" + booking.getId();
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            Tour tour = tourClientService.getTourById(bookingRequest.getTourId());
            model.addAttribute("tour", tour);
            model.addAttribute("scheduleId", bookingRequest.getScheduleId());
            return "page/booking/booking-form";
        }
    }

    // Trang xác nhận booking
    @GetMapping("/confirmation/{bookingId}")
    public String showBookingConfirmation(@PathVariable Long bookingId, Model model) {
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            model.addAttribute("booking", booking);
            return "page/booking/booking-confirmation";
        } catch (Exception e) {
            return "redirect:/page/tours";
        }
    }

    // Trang lịch sử booking của user
    @GetMapping("/history")
    public String showBookingHistory(@RequestParam Long userId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookings = bookingService.getBookingsByUser(userId, pageable);
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookings.getTotalPages());
        
        return "page/booking/booking-history";
    }

    // Hủy booking
    @PostMapping("/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId,
                               @RequestParam String reason,
                               RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(bookingId, reason);
            redirectAttributes.addFlashAttribute("success", "Hủy booking thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/page/booking/history";
    }

    // API tính tổng tiền (AJAX)
    @PostMapping("/calculate-total")
    @ResponseBody
    public String calculateTotal(@RequestParam Long tourId,
                                @RequestParam Integer adultCount,
                                @RequestParam Integer childCount,
                                @RequestParam Integer infantCount) {
        try {
            BigDecimal totalAmount = bookingService.calculateTotalAmount(tourId, adultCount, childCount, infantCount);
            BigDecimal depositAmount = bookingService.calculateDepositAmount(totalAmount);
            BigDecimal remainingAmount = totalAmount.subtract(depositAmount);
            
            return String.format("{\"totalAmount\":\"%,.0f\",\"depositAmount\":\"%,.0f\",\"remainingAmount\":\"%,.0f\"}", 
                totalAmount, depositAmount, remainingAmount);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    // API kiểm tra số chỗ còn lại
    @PostMapping("/check-availability")
    @ResponseBody
    public String checkAvailability(@RequestParam Long scheduleId,
                                   @RequestParam Integer totalGuests) {
        try {
            boolean available = bookingService.checkAvailability(scheduleId, totalGuests);
            return "{\"available\":" + available + "}";
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
} 