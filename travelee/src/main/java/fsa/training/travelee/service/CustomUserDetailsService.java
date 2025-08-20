package fsa.training.travelee.service;

import fsa.training.travelee.entity.User;
import fsa.training.travelee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username không tồn tại: " + username));

        if ("GOOGLE".equalsIgnoreCase(user.getProvider())) {
            throw new BadCredentialsException("Tài khoản này chỉ hỗ trợ đăng nhập bằng Google");
        }

        if (user.getPassword() == null) {
            throw new BadCredentialsException("Tài khoản chưa có mật khẩu");
        }

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),                     // username
                user.getPassword(),                  // encoded password
                "ACTIVE".equalsIgnoreCase(user.getStatus()), // enabled
                true,     // accountNonExpired
                true,     // credentialsNonExpired
                true,     // accountNonLocked
                authorities
        );
    }
}
