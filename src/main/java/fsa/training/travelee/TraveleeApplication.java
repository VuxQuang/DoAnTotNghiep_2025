package fsa.training.travelee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class TraveleeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TraveleeApplication.class, args);

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String rawPassword = "123456";
		String encodedPassword = encoder.encode(rawPassword);

		System.out.println("Mật khẩu 123456 sau khi mã hoá: " + encodedPassword);
	}

}
