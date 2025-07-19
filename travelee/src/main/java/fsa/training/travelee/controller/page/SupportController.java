package fsa.training.travelee.controller.page;

import fsa.training.travelee.entity.SupportRequest;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.service.SupportRequestService;
import fsa.training.travelee.service.UserService;
import fsa.training.travelee.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SupportController {

    private final SupportRequestService supportRequestService;

    private final UserService userService;

    @GetMapping("/page/contact")
    public String showContactPage(Model model) {
        model.addAttribute("supportRequest", new SupportRequest());
        return "page/contact";
    }



    @PostMapping("/page/contact/support")
    public String submitSupportRequest(
            @ModelAttribute("supportRequest") SupportRequest request,
            RedirectAttributes ra
    ) {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            request.setUser(currentUser);
        }
        supportRequestService.saveSupportRequest(request);
        ra.addFlashAttribute("success", "Cảm ơn bạn đã gửi liên hệ! Chúng tôi sẽ phản hồi sớm nhất.");
        return "redirect:/page/contact";
    }




}
