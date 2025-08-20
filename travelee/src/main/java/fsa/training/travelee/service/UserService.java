package fsa.training.travelee.service;

import fsa.training.travelee.dto.RegisterDto;
import fsa.training.travelee.dto.RegisterUserAdminDto;
import fsa.training.travelee.entity.User;
import org.springframework.data.domain.Page;

import jakarta.mail.MessagingException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.List;
import java.util.Optional;

public interface UserService {

    void sendResetPasswordEmail(String email, String token) throws MessagingException;

    void processOAuthPostLogin(String email, String fullName);

    void handleOAuth2Login(OAuth2AuthenticationToken authentication);

    String registerUser(RegisterDto dto);

    String generateToken(String email);

    boolean isTokenValid(String token);

    boolean resetPassword(String token, String newPassword);

    void createUserAdmin(RegisterUserAdminDto dto);

    void updateUserAdmin(RegisterUserAdminDto dto);

    User getCurrentUser();

    Page<User> getUsersPage(String keyword, int page, int size);

    void deleteUserById(Long id);

    Optional<User> findByEmail(String email);

//    List<User> searchUsers(String keyword);
}
