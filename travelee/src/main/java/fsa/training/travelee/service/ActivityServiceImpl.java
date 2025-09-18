package fsa.training.travelee.service;

import fsa.training.travelee.dto.RecentActivityDto;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.repository.BookingRepository;
import fsa.training.travelee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public List<RecentActivityDto> getRecentActivities(int limit) {
        List<RecentActivityDto> activities = new ArrayList<>();
        
        // Lấy user mới đăng ký gần đây
        List<User> recentUsers = userRepository.findAll().stream()
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .limit(5)
                .toList();
        for (User user : recentUsers) {
            activities.add(RecentActivityDto.builder()
                    .type("USER_REGISTER")
                    .title("Người dùng mới đăng ký")
                    .description(user.getFullName())
                    .createdAt(user.getCreatedAt())
                    .icon("fas fa-user-plus")
                    .timeAgo(getTimeAgo(user.getCreatedAt()))
                    .build());
        }
        
        // Lấy booking gần đây
        List<Booking> recentBookings = bookingRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .limit(10)
                .toList();
        for (Booking booking : recentBookings) {
            String title = getBookingTitle(booking);
            String description = getBookingDescription(booking);
            String icon = getBookingIcon(booking.getStatus());
            
            activities.add(RecentActivityDto.builder()
                    .type("BOOKING_CREATED")
                    .title(title)
                    .description(description)
                    .createdAt(booking.getCreatedAt())
                    .icon(icon)
                    .timeAgo(getTimeAgo(booking.getCreatedAt()))
                    .build());
        }
        
        // Sắp xếp theo thời gian tạo (mới nhất trước)
        activities.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // Giới hạn số lượng
        return activities.stream().limit(limit).toList();
    }
    
    private String getBookingTitle(Booking booking) {
        return switch (booking.getStatus()) {
            case PENDING -> "Đặt tour mới";
            case CONFIRMED -> "Tour đã xác nhận";
            case COMPLETED -> "Tour đã hoàn thành";
            case CANCELLED -> "Tour đã hủy";
            default -> "Cập nhật tour";
        };
    }
    
    private String getBookingDescription(Booking booking) {
        if (booking.getTour() != null) {
            return booking.getTour().getTitle();
        }
        return "Mã booking: " + booking.getBookingCode();
    }
    
    private String getBookingIcon(BookingStatus status) {
        return switch (status) {
            case PENDING -> "fas fa-calendar-plus";
            case CONFIRMED -> "fas fa-calendar-check";
            case COMPLETED -> "fas fa-check-circle";
            case CANCELLED -> "fas fa-times-circle";
            default -> "fas fa-calendar";
        };
    }
    
    private String getTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        
        if (minutes < 60) {
            return minutes <= 1 ? "Vừa xong" : minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else {
            return days + " ngày trước";
        }
    }
}
