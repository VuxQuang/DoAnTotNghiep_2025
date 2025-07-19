package fsa.training.travelee.controller.admin;

import fsa.training.travelee.dto.RegisterUserAdminDto;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.entity.Role;
import fsa.training.travelee.repository.UserRepository;
import fsa.training.travelee.service.UserService;
import fsa.training.travelee.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    private final UserService userService;

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        long totalUsers = userRepository.count();
        model.addAttribute("totalUsers", totalUsers);
        return "admin/dashboard";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/user/users")
    public String showAllUsers(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "5") int size,
                               Model model) {

        Page<User> usersPage = userService.getUsersPage(keyword, page, size);

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalUsers", usersPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        System.out.println("Total Users: " + usersPage.getTotalElements());

        return "admin/user/users";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/user/save")
    public String showCreateUserForm(Model model) {
        model.addAttribute("registerUserAdmin", new RegisterUserAdminDto());
        return "admin/user/create-user";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/user/save")
    public String saveUser(@ModelAttribute("registerUserAdmin") @Valid RegisterUserAdminDto dto,
                           BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isUpdate", dto.getId() != null);
            return "admin/user/create-user";
        }

        try {
            if (dto.getId() == null) {
                userService.createUserAdmin(dto);
            } else {
                userService.updateUserAdmin(dto);
            }
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isUpdate", dto.getId() != null);
            return "admin/user/create-user";
        }

        return "redirect:/admin/user/users";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/user/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        // Tìm user, nếu không thấy thì ném lỗi
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với id: " + id));

        RegisterUserAdminDto dto = new RegisterUserAdminDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setStatus(user.getStatus());

        dto.setRoleName(user.getRoles()
                .stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse(null));

        model.addAttribute("registerUserAdmin", dto);
        model.addAttribute("isUpdate", true); // để view biết đây là cập nhật

        return "admin/user/create-user"; // load form
    }

    @GetMapping("/admin/logout")
    public String logout() {
        return "redirect:/login";
    }

    @GetMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable Long id) {

        userService.deleteUserById(id);

        return "redirect:/admin/user/users";
    }
}
