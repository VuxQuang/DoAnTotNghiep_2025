package fsa.training.travelee.config;

import fsa.training.travelee.service.UserService;
import fsa.training.travelee.service.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Lazy
    private final UserService userService;
    private final HttpSession session;

    public LoginSuccessHandler(@Lazy  UserService userService, HttpSession session) {
        this.userService = userService;
        this.session = session;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String redirectURL = request.getContextPath();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oauthUser = oauthToken.getPrincipal();
            String email = oauthUser.getAttribute("email");
            String fullName = oauthUser.getAttribute("name");  // Lấy tên người dùng từ Google

            // Nếu email không có, redirect về trang login với thông báo lỗi
            if (email == null) {
                response.sendRedirect("/login?error=true");
                return;
            }

            // Gọi service để tạo user nếu chưa có và cập nhật thông tin nếu cần
            userService.processOAuthPostLogin(email, fullName);

            // Lưu thông tin vào session
            session.setAttribute("email", email);
            session.setAttribute("fullName", fullName);

            // Redirect cho user sau khi đăng nhập thành công
            redirectURL += "/page/home";
        } else {
            // Login bằng form
            String email = authentication.getName(); // Email là username
            String fullName = null;
            
            // Lấy thông tin user từ database
            try {
                var userOpt = userService.findByEmail(email);
                if (userOpt.isPresent()) {
                    var user = userOpt.get();
                    fullName = user.getFullName();
                    // Lưu thông tin vào session cho form login
                    session.setAttribute("email", email);
                    session.setAttribute("fullName", fullName);
                    if (user.getPhoneNumber() != null) {
                        session.setAttribute("phoneNumber", user.getPhoneNumber());
                    }
                }
            } catch (Exception e) {
                // Log error nhưng không dừng quá trình đăng nhập
                System.err.println("Error getting user info: " + e.getMessage());
            }
            
            var authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role) || "ROLE_STAFF".equals(role)) {
                    redirectURL += "/admin/dashboard";
                    break;
                } else if ("ROLE_USER".equals(role)) {
                    redirectURL += "/page/home";
                    break;
                }
            }
        }

        // Chuyển hướng sau khi xử lý đăng nhập thành công
        response.sendRedirect(redirectURL);
    }
}
