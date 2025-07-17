package fsa.training.travelee.controller.admin;

import fsa.training.travelee.entity.SupportRequest;
import fsa.training.travelee.entity.SupportStatus;
import fsa.training.travelee.service.SupportRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class SupportAdminController {

    @Autowired
    private SupportRequestService supportRequestService;

    @GetMapping("/admin/support")
    public String showSupport(Model model,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword) {
        Page<SupportRequest> supportRequests = supportRequestService.getSupportRequestsPage(keyword, page, size);

        model.addAttribute("supportRequests", supportRequests.getContent());
        model.addAttribute("totalItems", supportRequests.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", supportRequests.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        return "admin/support/support";
    }


    @GetMapping("/admin/support/{id}")
    public String showReplyForm(@PathVariable Long id, Model model) {
        SupportRequest supportRequest = supportRequestService.getById(id);
        model.addAttribute("supportRequest", supportRequest);
        return "admin/support/support";
    }

    @PostMapping("/admin/support/{id}/reply")
    public String replySupportRequest(@PathVariable Long id, @RequestParam String reply, Principal principal) {
        String replyBy = principal.getName();
        supportRequestService.replyToSupportRequest(id, reply, replyBy);
        return "redirect:/admin/support";  // Sau khi trả lời xong, quay lại danh sách yêu cầu hỗ trợ
    }

}
