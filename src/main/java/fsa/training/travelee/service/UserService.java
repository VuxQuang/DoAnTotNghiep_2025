package fsa.training.travelee.service;

import fsa.training.travelee.entity.Role;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.repository.RoleRepository;
import fsa.training.travelee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public void processOAuthPostLogin(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            Role userRole = roleRepository.findByRoleName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setProvider("GOOGLE");
            newUser.setStatus("ACTIVE");
            newUser.getRoles().add(userRole);

            userRepository.save(newUser);
        }
    }
}

