package fsa.training.travelee.repository;

import fsa.training.travelee.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByResetPasswordToken(String token);

    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String fullName, String email, String username
    );

    boolean existsByResetPasswordToken(String token);


    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phone);

    


}
