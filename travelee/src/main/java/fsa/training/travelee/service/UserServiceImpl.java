package fsa.training.travelee.service;

import fsa.training.travelee.dto.RegisterDto;
import fsa.training.travelee.dto.RegisterUserAdminDto;
import fsa.training.travelee.entity.Role;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.repository.RoleRepository;
import fsa.training.travelee.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendResetPasswordEmail(String email, String token) throws MessagingException {
        String resetPasswordUrl = "http://localhost:8080/reset-password?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("hotloan124@gmail.com");
        helper.setTo(email);
        helper.setSubject("Đặt lại mật khẩu");
        helper.setText("Vui lòng nhấp vào liên kết sau để đặt lại mật khẩu của bạn: <a href=\"" + resetPasswordUrl + "\">Đặt lại mật khẩu</a>", true);

        mailSender.send(message);
    }

    @Override
    public void processOAuthPostLogin(String email, String fullName) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            Role userRole = roleRepository.findByRoleName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

            User newUser = new User();
            newUser.setUsername(email);  // Dùng email làm username
            newUser.setEmail(email);
            newUser.setFullName(fullName);
            newUser.setProvider("GOOGLE");
            newUser.setStatus("ACTIVE");
            newUser.getRoles().add(userRole);

            userRepository.save(newUser);
        }
    }

    @Override
    public void handleOAuth2Login(OAuth2AuthenticationToken authentication) {
        OAuth2User oAuth2User = authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");

        processOAuthPostLogin(email, fullName);
    }

    @Override
    public String registerUser(RegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            return "Tài khoản đã tồn tại";
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus("ACTIVE");

        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        user.setRoles(new HashSet<>(Set.of(userRole)));

        userRepository.save(user);

        return "success";
    }

    @Override
    public String generateToken(String email) {
        String token = UUID.randomUUID().toString();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setResetPasswordToken(token);
        userRepository.save(user);

        return token;
    }

    @Override
    public boolean isTokenValid(String token) {
        return userRepository.existsByResetPasswordToken(token);
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        userRepository.save(user);
        return true;
    }

    @Override
    public void createUserAdmin(RegisterUserAdminDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role role = roleRepository.findByRoleName(dto.getRoleName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy role nào cả"));

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .status("ACTIVE")
                .provider("FORM")
                .build();

        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Override
    public void updateUserAdmin(RegisterUserAdminDto dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        if (!user.getFullName().equals(dto.getFullName())) {
            user.setFullName(dto.getFullName());
        }
        if (!user.getPhoneNumber().equals(dto.getPhoneNumber())) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        if (!user.getAddress().equals(dto.getAddress())) {
            user.setAddress(dto.getAddress());
        }
        if (!user.getStatus().equals(dto.getStatus())) {
            user.setStatus(dto.getStatus());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (!user.getRoles().contains(dto.getRoleName())) {
            Role role = roleRepository.findByRoleName(dto.getRoleName())
                    .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ"));
            user.setRoles(new HashSet<>(Set.of(role)));
        }

        userRepository.save(user);
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        Object principal = auth.getPrincipal();
        String username = null;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            username = oAuth2User.getAttribute("email");
        }

        return username == null ? null : userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public Page<User> getUsersPage(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.isEmpty()) {
            return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                    keyword, keyword, keyword, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

//    @Override
//    public List<User> searchUsers(String keyword) {
//        return userRepository.searchAllFields(keyword);
//    }
}
