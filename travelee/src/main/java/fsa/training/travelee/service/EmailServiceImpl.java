package fsa.training.travelee.service;

import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.entity.booking.BookingParticipant;
import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.entity.booking.ParticipantType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void sendBookingConfirmationEmail(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("hotloan124@gmail.com");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Xác nhận đặt tour - " + booking.getTour().getTitle());

            String emailContent = generateBookingConfirmationEmailContent(booking);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Đã gửi email xác nhận booking: {} đến {}", 
                    booking.getBookingCode(), booking.getUser().getEmail());
        } catch (MessagingException e) {
            log.error("Lỗi gửi email xác nhận booking: {}", e.getMessage());
        }
    }

    @Override
    public void sendBookingCancellationEmail(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("hotloan124@gmail.com");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Thông báo hủy tour - " + booking.getTour().getTitle());

            String emailContent = generateBookingCancellationEmailContent(booking);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Đã gửi email hủy booking: {} đến {}", 
                    booking.getBookingCode(), booking.getUser().getEmail());
        } catch (MessagingException e) {
            log.error("Lỗi gửi email hủy booking: {}", e.getMessage());
        }
    }

    @Override
    public void sendBookingStatusChangeEmail(Booking booking, String oldStatus, String newStatus) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("hotloan124@gmail.com");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Thay đổi trạng thái đặt tour - " + booking.getTour().getTitle());

            String emailContent = generateBookingStatusChangeEmailContent(booking, oldStatus, newStatus);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Đã gửi email thay đổi trạng thái booking: {} từ {} đến {} cho {}", 
                    booking.getBookingCode(), oldStatus, newStatus, booking.getUser().getEmail());
        } catch (MessagingException e) {
            log.error("Lỗi gửi email thay đổi trạng thái booking: {}", e.getMessage());
        }
    }

    private String generateBookingConfirmationEmailContent(Booking booking) {
        Context context = new Context(new Locale("vi", "VN"));
        context.setVariable("booking", booking);
        context.setVariable("tour", booking.getTour());
        context.setVariable("schedule", booking.getSchedule());
        context.setVariable("user", booking.getUser());
        context.setVariable("participants", booking.getParticipants());
        
        return templateEngine.process("email/booking-confirmation", context);
    }

    private String generateBookingCancellationEmailContent(Booking booking) {
        Context context = new Context(new Locale("vi", "VN"));
        context.setVariable("booking", booking);
        context.setVariable("tour", booking.getTour());
        context.setVariable("schedule", booking.getSchedule());
        context.setVariable("user", booking.getUser());
        context.setVariable("participants", booking.getParticipants());
        
        return templateEngine.process("email/booking-cancellation", context);
    }

    private String generateBookingStatusChangeEmailContent(Booking booking, String oldStatus, String newStatus) {
        Context context = new Context(new Locale("vi", "VN"));
        context.setVariable("booking", booking);
        context.setVariable("booking", booking);
        context.setVariable("tour", booking.getTour());
        context.setVariable("schedule", booking.getSchedule());
        context.setVariable("user", booking.getUser());
        context.setVariable("participants", booking.getParticipants());
        context.setVariable("oldStatus", oldStatus);
        context.setVariable("newStatus", newStatus);
        
        return templateEngine.process("email/booking-status-change", context);
    }

    @Override
    public void sendBookingCompletionEmail(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("hotloan124@gmail.com");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Cảm ơn bạn đã tham gia tour - " + booking.getTour().getTitle());

            String emailContent = generateBookingCompletionEmailContent(booking);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Đã gửi email cảm ơn hoàn thành tour booking: {} đến {}", 
                    booking.getBookingCode(), booking.getUser().getEmail());
        } catch (MessagingException e) {
            log.error("Lỗi gửi email cảm ơn hoàn thành tour: {}", e.getMessage());
        }
    }

    private String generateBookingCompletionEmailContent(Booking booking) {
        Context context = new Context(new Locale("vi", "VN"));
        context.setVariable("booking", booking);
        context.setVariable("tour", booking.getTour());
        context.setVariable("schedule", booking.getSchedule());
        context.setVariable("user", booking.getUser());
        context.setVariable("participants", booking.getParticipants());
        
        return templateEngine.process("email/booking-completion", context);
    }
    
    @Override
    public void sendBookingPaidEmail(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("hotloan124@gmail.com");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Thanh toán thành công - " + booking.getTour().getTitle());

            Context context = new Context(new Locale("vi", "VN"));
            context.setVariable("booking", booking);
            context.setVariable("tour", booking.getTour());
            context.setVariable("schedule", booking.getSchedule());
            context.setVariable("user", booking.getUser());
            context.setVariable("participants", booking.getParticipants());

            String emailContent = templateEngine.process("email/booking-paid", context);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Đã gửi email thanh toán thành công booking: {} đến {}",
                    booking.getBookingCode(), booking.getUser().getEmail());
        } catch (MessagingException e) {
            log.error("Lỗi gửi email thanh toán thành công: {}", e.getMessage());
        }
    }

    @Override
    public void sendBookingRefundEmail(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("hotloan124@gmail.com");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Hoàn tiền đặt tour - " + booking.getTour().getTitle());

            Context context = new Context(new Locale("vi", "VN"));
            context.setVariable("booking", booking);
            context.setVariable("tour", booking.getTour());
            context.setVariable("schedule", booking.getSchedule());
            context.setVariable("user", booking.getUser());
            context.setVariable("participants", booking.getParticipants());

            String emailContent = templateEngine.process("email/booking-refund", context);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Đã gửi email hoàn tiền booking: {} đến {}",
                    booking.getBookingCode(), booking.getUser().getEmail());
        } catch (MessagingException e) {
            log.error("Lỗi gửi email hoàn tiền: {}", e.getMessage());
        }
    }
}
