package fsa.training.travelee.config;

import fsa.training.travelee.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService; // service bạn phải inject vào để xử lý lưu user
    private final HttpSession session;

    public LoginSuccessHandler(UserService userService, HttpSession session) {
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

            // Nếu email không có thì lỗi
            if (email == null) {
                response.sendRedirect("/login?error");
                return;
            }

            // Gọi service để tạo user nếu chưa có
            userService.processOAuthPostLogin(email);

            // Có thể lưu vào session nếu muốn
            session.setAttribute("username", email);

            // Redirect cho user
            redirectURL += "/page/home";
        } else {
            // Login bằng form
            var authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_ADMIN")) {
                    redirectURL += "/admin/dashboard";
                    break;
                } else if (role.equals("ROLE_USER")) {
                    redirectURL += "/page/home";
                    break;
                }
            }
        }

        response.sendRedirect(redirectURL);
    }
}
