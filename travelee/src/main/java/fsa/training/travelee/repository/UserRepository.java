package fsa.training.travelee.repository;

import fsa.training.travelee.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByResetPasswordToken(String token);

    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String fullName, String email, String username, Pageable pageable
    );

    boolean existsByResetPasswordToken(String token);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

}
