# Email Service cho Booking - TRAVELEE

## Tổng quan

Email Service được tạo để tự động gửi email thông báo cho khách hàng khi có thay đổi về trạng thái đặt tour.

## Các loại email được hỗ trợ

### 1. Email xác nhận đặt tour
- **Khi nào gửi**: Khi admin xác nhận booking từ trạng thái PENDING
- **Nội dung**: Thông tin xác nhận tour, chi tiết booking, danh sách thành viên
- **Template**: `src/main/resources/templates/email/booking-confirmation.html`

### 2. Email hủy tour
- **Khi nào gửi**: 
  - Khi khách hàng tự hủy booking
  - Khi admin hủy booking
- **Nội dung**: Thông báo hủy tour, chi tiết booking, danh sách thành viên
- **Template**: `src/main/resources/templates/email/booking-cancellation.html`

### 3. Email thay đổi trạng thái
- **Khi nào gửi**: Không sử dụng (đã bỏ)
- **Nội dung**: Không áp dụng

### 4. Email hoàn thành tour 🆕
- **Khi nào gửi**: Khi admin đánh dấu tour đã hoàn thành (COMPLETED)
- **Nội dung**: Lời cảm ơn khách hàng, mong muốn họ tiếp tục ủng hộ, thông tin liên hệ hỗ trợ
- **Template**: `src/main/resources/templates/email/booking-completion.html`
- **Template**: `src/main/resources/templates/email/booking-status-change.html`

## Cấu hình

### 1. Cấu hình SMTP (application.properties)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
```

### 2. Dependencies cần thiết
- `spring-boot-starter-mail`: Gửi email
- `spring-boot-starter-thymeleaf`: Template engine
- `thymeleaf-extras-springsecurity6`: Security integration

## Cách sử dụng

### 1. Trong BookingService
```java
@Service
public class BookingServiceImpl implements BookingService {
    
    private final EmailService emailService;
    
    // Gửi email xác nhận
    emailService.sendBookingConfirmationEmail(booking);
    
    // Gửi email hủy
    emailService.sendBookingCancellationEmail(booking);
    
    // Gửi email cảm ơn hoàn thành tour
    emailService.sendBookingCompletionEmail(booking);
}
```

### 2. Gọi trực tiếp từ controller
```java
@Autowired
private EmailService emailService;

@PostMapping("/cancel")
public String cancelBooking(@RequestParam Long bookingId) {
    Booking booking = bookingService.getBookingById(bookingId);
    emailService.sendBookingCancellationEmail(booking);
    return "redirect:/bookings";
}
```

## Luồng hoạt động

### 1. Khách hàng hủy booking
1. Gọi `cancelBooking()` trong `BookingService`
2. Cập nhật trạng thái thành `CANCELLED`
3. Gửi email hủy qua `EmailService`

### 2. Admin thay đổi trạng thái
1. Gọi `updateBookingStatus()` trong `BookingService`
2. Nếu là `CANCELLED`: gọi `cancelBooking()` (đã xử lý email)
3. Nếu là `CONFIRMED` từ `PENDING`: gửi email xác nhận
4. Nếu là `COMPLETED`: gửi email cảm ơn hoàn thành tour
5. Các thay đổi trạng thái khác: không gửi email

### 3. Admin hủy booking
1. Gọi `cancelBooking()` trong `BookingService`
2. Cập nhật trạng thái và lý do hủy
3. Gửi email hủy

## Template Email

### Cấu trúc template
- Sử dụng Thymeleaf syntax
- Responsive design với CSS inline
- Hỗ trợ tiếng Việt (UTF-8)
- Thông tin booking, tour, schedule, participants

### Biến có sẵn trong template
- `booking`: Thông tin booking
- `tour`: Thông tin tour
- `schedule`: Lịch trình tour
- `user`: Thông tin khách hàng
- `participants`: Danh sách thành viên
- `oldStatus`, `newStatus`: Trạng thái cũ và mới (chỉ cho status change)

## Xử lý lỗi

### 1. Lỗi gửi email
- Log lỗi chi tiết
- Không ảnh hưởng đến luồng chính
- Có thể retry sau

### 2. Lỗi template
- Sử dụng template mặc định
- Log lỗi để debug

## Monitoring và Log

### 1. Log thành công
```
Đã gửi email xác nhận booking: BK123456789 đến user@example.com
Đã gửi email hủy booking: BK123456789 đến user@example.com
```

### 2. Log lỗi
```
Lỗi gửi email xác nhận booking: Connection timeout
Lỗi gửi email hủy booking: Authentication failed
```

## Tùy chỉnh

### 1. Thay đổi template
- Chỉnh sửa file HTML trong `src/main/resources/templates/email/`
- Cập nhật CSS và layout
- Thêm/bớt thông tin hiển thị

### 2. Thay đổi nội dung email
- Cập nhật text trong template
- Thay đổi subject line trong `EmailServiceImpl`
- Tùy chỉnh format ngày tháng, tiền tệ

### 3. Thêm loại email mới
1. Tạo template HTML mới
2. Thêm method trong `EmailService` interface
3. Implement trong `EmailServiceImpl`
4. Gọi từ service cần thiết

## Troubleshooting

### 1. Email không gửi được
- Kiểm tra cấu hình SMTP
- Kiểm tra username/password
- Kiểm tra firewall/network

### 2. Template không render
- Kiểm tra syntax Thymeleaf
- Kiểm tra đường dẫn template
- Kiểm tra encoding UTF-8

### 3. Email bị spam
- Cấu hình SPF, DKIM
- Sử dụng email domain thay vì Gmail
- Tránh từ khóa spam trong subject/content

## Bảo mật

### 1. Thông tin nhạy cảm
- Không gửi mật khẩu qua email
- Mã hóa thông tin cá nhân nếu cần
- Tuân thủ GDPR/CCPA

### 2. Rate limiting
- Giới hạn số email gửi/phút
- Tránh spam cho khách hàng
- Monitoring email queue

## Performance

### 1. Async email
- Có thể implement async để không block main thread
- Sử dụng `@Async` annotation
- Queue email để xử lý batch

### 2. Template caching
- Thymeleaf cache template
- Pre-compile template nếu cần
- Optimize CSS/HTML size
