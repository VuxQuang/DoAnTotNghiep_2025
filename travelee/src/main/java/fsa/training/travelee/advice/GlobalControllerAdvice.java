package fsa.training.travelee.advice;

import fsa.training.travelee.entity.User;
import fsa.training.travelee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    @ModelAttribute
    public void addLoggedInUser(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = null;

            // Trường hợp đăng nhập bằng form (UserDetails)
            if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
            }

            // Trường hợp đăng nhập bằng Google OAuth2
            else if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                OAuth2User oauthUser = (OAuth2User) oauthToken.getPrincipal();
                username = oauthUser.getAttribute("email");  // Email là username trong DB
            }

            if (username != null) {
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    model.addAttribute("loggedInUser", user);
                }
            }
        }
    }
}
