package fsa.training.travelee.controller.page;

import fsa.training.travelee.dto.ChangePasswordDto;
import fsa.training.travelee.dto.UpdateUserDto;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.repository.UserRepository;
import fsa.training.travelee.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class PageController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping({"/","", "/home","/page/home"})
    public String showPageHome(Model model) {
        User currentUser = userService.getCurrentUser();

        if (currentUser != null) {
            model.addAttribute("fullName", currentUser.getFullName());
            model.addAttribute("loggedInUser", currentUser);
        }

        return "page/home";
    }

    @GetMapping("/oauth2/authorization/google")
    public String showGoogleLogin() {
        return "oauth2/authorization/google";
    }

    @GetMapping("/profile")
    public String showProfileForm(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UpdateUserDto dto = new UpdateUserDto();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());

        model.addAttribute("updateUserDto", dto);
        return "/page/profile";
    }

    @PostMapping("/profile/update")
    public String updateUser(@ModelAttribute("updateUserDto") UpdateUserDto dto, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            user.setAddress(dto.getAddress());
        }

        userRepository.save(user);
        return "redirect:/page/home";
    }
    @PostMapping("/profile/change-password")
    public String changePassword(@ModelAttribute ChangePasswordDto dto, Principal principal, RedirectAttributes ra) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            ra.addFlashAttribute("error", "Mật khẩu hiện tại không đúng.");
            return "redirect:/profile";
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            ra.addFlashAttribute("error", "Mật khẩu mới không trùng khớp.");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        ra.addFlashAttribute("success", "Đổi mật khẩu thành công.");
        return "redirect:/profile?tab=password";
    }
    @GetMapping("/page/about")
    public String showPageAbout() {
        return "page/about";
    }

}
