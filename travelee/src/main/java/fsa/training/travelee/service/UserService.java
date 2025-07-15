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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

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

    public void processOAuthPostLogin(String email, String fullName) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            // Lấy role user
            Role userRole = roleRepository.findByRoleName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

            // Tạo người dùng mới
            User newUser = new User();
            newUser.setUsername(email);  // Dùng email làm username
            newUser.setEmail(email);
            newUser.setFullName(fullName);  // Lưu tên người dùng từ Google
            newUser.setProvider("GOOGLE");  // Đặt provider là "GOOGLE"
            newUser.setStatus("ACTIVE");   // Đặt trạng thái người dùng là ACTIVE
            newUser.getRoles().add(userRole);

            // Lưu người dùng vào cơ sở dữ liệu
            userRepository.save(newUser);
        }
    }
    public void handleOAuth2Login(OAuth2AuthenticationToken authentication) {
        // Lấy thông tin người dùng từ Google OAuth2
        OAuth2User oAuth2User = authentication.getPrincipal();

        // Lấy email và tên người dùng từ Google
        String email = oAuth2User.getAttribute("email");  // Lấy email của người dùng
        String fullName = oAuth2User.getAttribute("name");  // Lấy tên người dùng từ Google

        // Gọi phương thức để xử lý đăng nhập và lưu người dùng vào cơ sở dữ liệu
        processOAuthPostLogin(email, fullName);
    }

    public String registerUser(RegisterDto dto) {
        // Kiểm tra xem email đã tồn tại chưa
        if (userRepository.existsByUsername(dto.getUsername())) {
            return "Tài khoản đã tồn tại";
        }

        // Tạo đối tượng User từ DTO
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus("ACTIVE"); // Hoặc "INACTIVE"

        // Gán vai trò mặc định ROLE_USER
        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        user.setRoles(new HashSet<>(Set.of(userRole)));

        // Lưu người dùng vào DB
        userRepository.save(user);

        return "success";
    }

    public String generateToken(String email) {
        // Tạo token ngẫu nhiên
        String token = UUID.randomUUID().toString();

        // Lấy người dùng từ email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Gán token vào người dùng
        user.setResetPasswordToken(token);

        // Lưu token vào cơ sở dữ liệu
        userRepository.save(user);

        return token;
    }

    public boolean isTokenValid(String token) {
        return userRepository.existsByResetPasswordToken(token); // Kiểm tra token trong DB
    }

    // Cập nhật mật khẩu người dùng khi đã xác nhận token
    public boolean resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);  // Xóa token sau khi cập nhật mật khẩu
        userRepository.save(user);
        return true;
    }

    public void createUserAdmin(RegisterUserAdminDto dto){
        if(userRepository.existsByUsername(dto.getUsername())){
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if(userRepository.existsByEmail(dto.getEmail())){
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role role = roleRepository.findByRoleName(dto.getRoleName()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy role nào cả"));

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

    public void updateUserAdmin(RegisterUserAdminDto dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (!user.getEmail().equals(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        /* --- Cập nhật các trường --- */
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        user.setStatus(dto.getStatus());

        /* --- Cập nhật mật khẩu (nếu nhập mới) --- */
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        /* --- Cập nhật role --- */
        Role role = roleRepository.findByRoleName(dto.getRoleName())
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ"));
        user.setRoles(new HashSet<>(Set.of(role)));

        userRepository.save(user);
    }
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
            username = oAuth2User.getAttribute("email"); // login bằng Google → username là email
        }

        if (username == null) return null;

        return userRepository.findByUsername(username).orElse(null);
    }

}

