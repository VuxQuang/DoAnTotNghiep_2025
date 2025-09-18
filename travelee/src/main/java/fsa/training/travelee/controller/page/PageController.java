package fsa.training.travelee.controller.page;

import fsa.training.travelee.dto.ChangePasswordDto;
import fsa.training.travelee.dto.UpdateUserDto;
import fsa.training.travelee.entity.SupportRequest;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.repository.UserRepository;
import fsa.training.travelee.service.UserService;
import fsa.training.travelee.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;

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

    @GetMapping("/page/about")
    public String showPageAbout() {
        return "page/about";
    }


}
