package fsa.training.travelee.controller;

import fsa.training.travelee.dto.RegisterDto;
import fsa.training.travelee.service.UserService;
import fsa.training.travelee.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/register")
    public String handleRegister(@ModelAttribute("user") RegisterDto dto, Model model){
        String result = userService.registerUser(dto);

        if (!"success".equals(result)) {
            model.addAttribute("error", result);
            return "register";
        }
        return "redirect:/login?success=true";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email, Model model) {
        // Tạo token reset mật khẩu
        String token = userService.generateToken(email);  // Bạn có thể tạo token ngẫu nhiên ở đây

        // Gửi email với link reset mật khẩu
        try {
            userService.sendResetPasswordEmail(email, token);  // Gửi email với token
            model.addAttribute("message", "Một email chứa liên kết reset mật khẩu đã được gửi.");
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi gửi email: " + e.getMessage());
        }

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        // Kiểm tra token hợp lệ từ URL
        if (!userService.isTokenValid(token)) {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn.");
            return "error";  // Nếu token không hợp lệ, chuyển hướng đến trang lỗi
        }

        // Nếu token hợp lệ, trả về form reset mật khẩu
        model.addAttribute("token", token);
        return "reset-password";  // Trang nhập mật khẩu mới
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Model model) {
        // Kiểm tra mật khẩu khớp
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu không khớp.");
            return "reset-password";
        }

        // Cập nhật mật khẩu mới nếu token hợp lệ
        boolean isReset = userService.resetPassword(token, newPassword);
        if (isReset) {
            model.addAttribute("message", "Mật khẩu đã được thay đổi thành công.");
            return "login";  // Chuyển hướng về trang đăng nhập
        } else {
            model.addAttribute("error", "Có lỗi xảy ra khi cập nhật mật khẩu.");
            return "reset-password";
        }
    }

}
