package fsa.training.travelee.controller.admin;

import fsa.training.travelee.dto.RegisterUserAdminDto;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.entity.Role;
import fsa.training.travelee.repository.UserRepository;
import fsa.training.travelee.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final TourService tourService;
    private final BookingService bookingService;
    private final ActivityService activityService;

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        long totalUsers = userRepository.count();
        long totalTours = tourService.countTours();
        long todayBookings = bookingService.countTodayBookings();
        
        // Lấy thống kê tháng hiện tại
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        
        long monthlyBookings = bookingService.countBookingsByMonth(currentYear, currentMonth);
        java.math.BigDecimal monthlyRevenue = bookingService.getRevenueByMonth(currentYear, currentMonth);
        java.util.List<fsa.training.travelee.dto.MonthlyBookingStatsDto> monthlyStats = bookingService.getMonthlyBookingStats(currentYear, currentMonth);
        
        System.out.println("=== DASHBOARD DEBUG ===");
        System.out.println("Current Year: " + currentYear + ", Current Month: " + currentMonth);
        System.out.println("Monthly Bookings: " + monthlyBookings);
        System.out.println("Monthly Revenue: " + monthlyRevenue);
        System.out.println("Monthly Stats Count: " + monthlyStats.size());
        
        // Debug: Kiểm tra tất cả booking trong tháng 8 (bất kể status)
        long allBookingsInAugust = bookingService.countBookingsByMonth(2025, 8);
        System.out.println("All bookings in August 2025: " + allBookingsInAugust);
        
        // Debug: Kiểm tra booking theo status
        long completedBookings = bookingService.getTotalBookingsByStatus(fsa.training.travelee.entity.booking.BookingStatus.COMPLETED);
        long pendingBookings = bookingService.getTotalBookingsByStatus(fsa.training.travelee.entity.booking.BookingStatus.PENDING);
        long cancelledBookings = bookingService.getTotalBookingsByStatus(fsa.training.travelee.entity.booking.BookingStatus.CANCELLED);
        
        System.out.println("Completed bookings: " + completedBookings);
        System.out.println("Pending bookings: " + pendingBookings);
        System.out.println("Cancelled bookings: " + cancelledBookings);
        
        // Debug: Kiểm tra booking ngày 16/9/2025
        long bookingsOn16Sep = bookingService.countBookingsByDate(2025, 9, 16);
        System.out.println("Bookings on 16/9/2025: " + bookingsOn16Sep);
        
        // Debug: Kiểm tra tất cả booking trong tháng 9/2025
        long allBookingsInSeptember = bookingService.countBookingsByMonth(2025, 9);
        System.out.println("All bookings in September 2025: " + allBookingsInSeptember);
        
        System.out.println("======================");
        
        // Lấy hoạt động gần đây
        var recentActivities = activityService.getRecentActivities(5);
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalTours", totalTours);
        model.addAttribute("todayBookings", todayBookings);
        model.addAttribute("monthlyBookings", monthlyBookings);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("monthlyStats", monthlyStats);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("recentActivities", recentActivities);
        
        return "admin/dashboard";
    }

    @GetMapping("/admin/dashboard/monthly-stats")
    @ResponseBody
    public java.util.Map<String, Object> getMonthlyStats(@RequestParam int year, @RequestParam int month) {
        long monthlyBookings = bookingService.countBookingsByMonth(year, month);
        java.math.BigDecimal monthlyRevenue = bookingService.getRevenueByMonth(year, month);
        java.util.List<fsa.training.travelee.dto.MonthlyBookingStatsDto> monthlyStats = bookingService.getMonthlyBookingStats(year, month);
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("monthlyBookings", monthlyBookings);
        response.put("monthlyRevenue", monthlyRevenue);
        response.put("monthlyStats", monthlyStats);
        response.put("year", year);
        response.put("month", month);
        
        return response;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/user/users")
    public String showAllUsers(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "5") int size,
                               Model model) {

        Page<User> usersPage = userService.getUsersPage(keyword, page, size);

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalUsers", usersPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        System.out.println("Total Users: " + usersPage.getTotalElements());

        return "admin/user/users";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/user/save")
    public String showCreateUserForm(Model model) {
        model.addAttribute("registerUserAdmin", new RegisterUserAdminDto());
        return "admin/user/create-user";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/user/save")
    public String saveUser(@ModelAttribute("registerUserAdmin") @Valid RegisterUserAdminDto dto,
                           BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isUpdate", dto.getId() != null);
            return "admin/user/create-user";
        }

        try {
            if (dto.getId() == null) {
                userService.createUserAdmin(dto);
            } else {
                userService.updateUserAdmin(dto);
            }
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isUpdate", dto.getId() != null);
            return "admin/user/create-user";
        }

        return "redirect:/admin/user/users";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/user/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        // Tìm user, nếu không thấy thì ném lỗi
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với id: " + id));

        RegisterUserAdminDto dto = new RegisterUserAdminDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setStatus(user.getStatus());

        dto.setRoleName(user.getRoles()
                .stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse(null));

        model.addAttribute("registerUserAdmin", dto);
        model.addAttribute("isUpdate", true); // để view biết đây là cập nhật

        return "admin/user/create-user"; // load form
    }

    @GetMapping("/admin/logout")
    public String logout() {
        return "redirect:/login";
    }

    @GetMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable Long id) {

        userService.deleteUserById(id);

        return "redirect:/admin/user/users";
    }

//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @GetMapping("/admin/user/users/list")
//    public String searchUser(@RequestParam("keyword") String keyword, Model model){
//        List<User> users = userService.searchUsers(keyword);
//        model.addAttribute("users", users);
//
//        return "admin/user/users";
//    }
}
