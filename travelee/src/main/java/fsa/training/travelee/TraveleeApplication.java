package fsa.training.travelee;

import fsa.training.travelee.entity.Role;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.repository.RoleRepository;
import fsa.training.travelee.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class TraveleeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TraveleeApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(RoleRepository roleRepository, 
                                    UserRepository userRepository, 
                                    PasswordEncoder passwordEncoder) {
		return args -> {
			// Khởi tạo roles
			Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
					.orElseGet(() -> {
						Role role = new Role();
						role.setRoleName("ROLE_ADMIN");
						return roleRepository.save(role);
					});

			Role userRole = roleRepository.findByRoleName("ROLE_USER")
					.orElseGet(() -> {
						Role role = new Role();
						role.setRoleName("ROLE_USER");
						return roleRepository.save(role);
					});

			Role staffRole = roleRepository.findByRoleName("ROLE_STAFF")
					.orElseGet(() -> {
						Role role = new Role();
						role.setRoleName("ROLE_STAFF");
						return roleRepository.save(role);
					});

			// Tạo tài khoản admin nếu chưa có
			if (!userRepository.existsByUsername("admin")) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setEmail("admin@travelee.com");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setFullName("Administrator");
				admin.setStatus("ACTIVE");
				admin.setProvider("FORM");
				admin.setCreatedAt(LocalDateTime.now());
				admin.setUpdatedAt(LocalDateTime.now());
				
				Set<Role> adminRoles = new HashSet<>();
				adminRoles.add(adminRole);
				admin.setRoles(adminRoles);
				
				userRepository.save(admin);
				System.out.println("Đã tạo tài khoản admin: admin/admin123");
			}

			// Tạo tài khoản staff nếu chưa có
			if (!userRepository.existsByUsername("staff")) {
				User staff = new User();
				staff.setUsername("staff");
				staff.setEmail("staff@travelee.com");
				staff.setPassword(passwordEncoder.encode("staff123"));
				staff.setFullName("Staff Member");
				staff.setStatus("ACTIVE");
				staff.setProvider("FORM");
				staff.setCreatedAt(LocalDateTime.now());
				staff.setUpdatedAt(LocalDateTime.now());
				
				Set<Role> staffRoles = new HashSet<>();
				staffRoles.add(staffRole);
				staff.setRoles(staffRoles);
				
				userRepository.save(staff);
				System.out.println("Đã tạo tài khoản staff: staff/staff123");
			}
		};
	}
}
