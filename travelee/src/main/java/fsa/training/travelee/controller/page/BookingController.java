package fsa.training.travelee.controller.page;

import fsa.training.travelee.dto.booking.BookingRequestDto;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.entity.TourSchedule;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.service.BookingService;
import fsa.training.travelee.service.TourClientService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/page/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final TourClientService tourClientService;
    private final fsa.training.travelee.service.UserService userService;

    // Hiển thị form booking
    @GetMapping("/{tourId}")
    public String showBookingForm(@PathVariable Long tourId,
                                 @RequestParam(required = false) Long scheduleId,
                                 Model model) {
        
        // Lấy tour
        Tour tour = tourClientService.getTourById(tourId);
        if (tour == null) {
            return "redirect:/page/tours/newest";
        }
        
        // Kiểm tra schedule
        TourSchedule selectedSchedule = null;
        if (scheduleId != null) {
            selectedSchedule = tour.getSchedules().stream()
                    .filter(schedule -> schedule.getId().equals(scheduleId))
                    .findFirst()
                    .orElse(null);
        }
        
        // Nếu không có schedule được chọn, lấy schedule đầu tiên
        if (selectedSchedule == null && !tour.getSchedules().isEmpty()) {
            selectedSchedule = tour.getSchedules().iterator().next();
        }
        
        // Lấy user từ SecurityContextHolder
        User user = userService.getCurrentUser();
        BookingRequestDto bookingRequest = new BookingRequestDto();
        if (user != null) {
            bookingRequest.setCustomerName(user.getFullName());
            bookingRequest.setCustomerEmail(user.getEmail());
            bookingRequest.setCustomerPhone(user.getPhoneNumber());
        }
        bookingRequest.setTourId(tour.getId());
        if (selectedSchedule != null) {
            bookingRequest.setScheduleId(selectedSchedule.getId());
        }
        
        model.addAttribute("tour", tour);
        model.addAttribute("selectedSchedule", selectedSchedule);
        model.addAttribute("user", user);
        model.addAttribute("bookingRequest", bookingRequest);
        
        return "page/booking/booking-form";
    }

    // Endpoint thay thế cho /booking-detail
    @GetMapping("/detail")
    public String showBookingDetail(@RequestParam Long tourId,
                                   @RequestParam(required = false) Long scheduleId,
                                   Model model) {
        
        // Lấy tour
        Tour tour = tourClientService.getTourById(tourId);
        if (tour == null) {
            return "redirect:/page/tours/newest";
        }
        
        // Kiểm tra schedule
        TourSchedule selectedSchedule = null;
        if (scheduleId != null) {
            selectedSchedule = tour.getSchedules().stream()
                    .filter(schedule -> schedule.getId().equals(scheduleId))
                    .findFirst()
                    .orElse(null);
        }
        
        // Nếu không có schedule được chọn, lấy schedule đầu tiên
        if (selectedSchedule == null && !tour.getSchedules().isEmpty()) {
            selectedSchedule = tour.getSchedules().iterator().next();
        }
        
        // Lấy user từ SecurityContextHolder
        User user = userService.getCurrentUser();
        BookingRequestDto bookingRequest = new BookingRequestDto();
        if (user != null) {
            bookingRequest.setCustomerName(user.getFullName());
            bookingRequest.setCustomerEmail(user.getEmail());
            bookingRequest.setCustomerPhone(user.getPhoneNumber());
        }
        bookingRequest.setTourId(tour.getId());
        if (selectedSchedule != null) {
            bookingRequest.setScheduleId(selectedSchedule.getId());
        }
        
        model.addAttribute("tour", tour);
        model.addAttribute("selectedSchedule", selectedSchedule);
        model.addAttribute("user", user);
        model.addAttribute("bookingRequest", bookingRequest);
        
        return "page/booking/booking-form";
    }

    // Xử lý tạo booking
    @PostMapping("/create")
    public String createBooking(@Valid @ModelAttribute BookingRequestDto bookingRequest,
                               BindingResult bindingResult,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            // Lấy lại thông tin tour và schedule để hiển thị lại form
            Tour tour = tourClientService.getTourById(bookingRequest.getTourId());
            TourSchedule selectedSchedule = tour.getSchedules().stream()
                    .filter(schedule -> schedule.getId().equals(bookingRequest.getScheduleId()))
                    .findFirst()
                    .orElse(null);
            
            User user = userService.getCurrentUser();
            
            model.addAttribute("tour", tour);
            model.addAttribute("selectedSchedule", selectedSchedule);
            model.addAttribute("user", user);
            model.addAttribute("bookingRequest", bookingRequest);
            
            return "page/booking/booking-form";
        }
        
        try {
            // Lấy user từ session
            User user = userService.getCurrentUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đặt tour");
                return "redirect:/login";
            }
            
            // Tạo booking
            Booking booking = bookingService.createBooking(bookingRequest, user);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Đặt tour thành công! Mã booking: " + booking.getBookingCode());
            redirectAttributes.addFlashAttribute("booking", booking);
            
            return "redirect:/page/booking/confirmation/" + booking.getId();
            
        } catch (Exception e) {
            log.error("Lỗi tạo booking", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/page/booking/" + bookingRequest.getTourId() + 
                   "?scheduleId=" + bookingRequest.getScheduleId();
        }
    }

    // Hiển thị trang xác nhận booking
    @GetMapping("/confirmation/{bookingId}")
    public String showBookingConfirmation(@PathVariable Long bookingId,
                                         Model model,
                                         HttpSession session) {
        
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                return "redirect:/page/tours/newest";
            }
            
            // Kiểm tra quyền xem booking
            User user = userService.getCurrentUser();
            if (user == null || !booking.getUser().getId().equals(user.getId())) {
                return "redirect:/login";
            }
            
            // Đảm bảo các relationship được load
            if (booking.getTour() == null || booking.getSchedule() == null || booking.getUser() == null) {
                log.error("Booking {} thiếu thông tin relationship", bookingId);
                return "redirect:/page/tours/newest";
            }
            
            model.addAttribute("booking", booking);
            model.addAttribute("user", user);
            return "page/booking/booking-confirmation";
            
        } catch (Exception e) {
            log.error("Lỗi hiển thị trang xác nhận booking: {}", e.getMessage());
            return "redirect:/page/tours/newest";
        }
    }

    // API tính tổng tiền (AJAX)
    @PostMapping("/calculate-price")
    @ResponseBody
    public BigDecimal calculatePrice(@RequestParam Long tourId,
                                   @RequestParam Long scheduleId,
                                   @RequestParam int adultCount,
                                   @RequestParam int childCount) {
        try {
            return bookingService.calculateTotalAmount(tourId, scheduleId, adultCount, childCount);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // API kiểm tra availability (AJAX)
    @PostMapping("/check-availability")
    @ResponseBody
    public boolean checkAvailability(@RequestParam Long scheduleId,
                                   @RequestParam int adultCount,
                                   @RequestParam int childCount) {
        try {
            return bookingService.isScheduleAvailable(scheduleId, adultCount, childCount);
        } catch (Exception e) {
            return false;
        }
    }

    // Hiển thị lịch sử booking của user
    @GetMapping("/history")
    public String showBookingHistory(Model model,
                                   HttpSession session,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var bookings = bookingService.getBookingsByUser(user, pageable);
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", user);
        
        return "redirect:/profile?tab=tours";
    }

    // Hủy booking
    @PostMapping("/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId,
                               @RequestParam String reason,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.getCurrentUser();
            if (user == null) {
                return "redirect:/login";
            }
            
            Booking booking = bookingService.getBookingById(bookingId);
            if (!booking.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy booking này");
                return "redirect:/page/booking/history";
            }
            
            bookingService.cancelBooking(bookingId, reason);
            redirectAttributes.addFlashAttribute("success", "Đã hủy booking thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/page/booking/history";
    }
} 