package fsa.training.travelee.controller;

import fsa.training.travelee.dto.RegisterUserAdminDto;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.entity.Role;
import fsa.training.travelee.repository.UserRepository;
import fsa.training.travelee.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // <- cần import
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


import java.security.Principal;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/user/users")
    public String showAllUsers(@RequestParam(value = "keyword", required = false) String keyword,
                               Model model) {
        List<User> users;

        if (keyword != null && !keyword.isEmpty()) {
            users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                    keyword, keyword, keyword
            );
        } else {
            users = userRepository.findAll();
        }

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword); // để giữ lại keyword trên ô input nếu cần
        return "admin/user/users";
    }


    @GetMapping("/admin/user/save")
    public String showCreateUserForm(Model model) {
        model.addAttribute("registerUserAdmin", new RegisterUserAdminDto());
        return "admin/user/create-user";
    }


    @PostMapping("/admin/user/save")
    public String saveUser(@ModelAttribute("registerUserAdmin") @Valid RegisterUserAdminDto dto,
                           BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isUpdate", dto.getId() != null);
            return "admin/user/create-user";
        }

        try {
            if (dto.getId() == null) {
                userService.createUserAdmin(dto);   // tạo mới
            } else {
                userService.updateUserAdmin(dto);   // cập nhật
            }
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isUpdate", dto.getId() != null);
            return "admin/user/create-user";
        }

        return "redirect:/admin/user/users";
    }
    @GetMapping("/admin/user/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        // Tìm user, nếu không thấy thì ném lỗi
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với id: " + id));

        // Tạo DTO từ entity
        RegisterUserAdminDto dto = new RegisterUserAdminDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setStatus(user.getStatus());

        // Lấy role đầu tiên (nếu có)
        dto.setRoleName(user.getRoles()
                .stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse(null));

        model.addAttribute("registerUserAdmin", dto);
        model.addAttribute("isUpdate", true); // để view biết đây là cập nhật

        return "admin/user/create-user"; // load form
    }

}
