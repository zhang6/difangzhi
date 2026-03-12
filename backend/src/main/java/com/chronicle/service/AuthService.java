package com.chronicle.service;

import com.chronicle.dto.LoginRequest;
import com.chronicle.dto.LoginResponse;
import com.chronicle.entity.YbUser;
import com.chronicle.repository.UserRepository;
import com.chronicle.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        var user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        String token = jwtService.generateToken(user.getUsername());
        return LoginResponse.of(token, user);
    }

    public YbUser initDefaultUsers() {
        if (!userRepository.existsByUsername("admin")) {
            YbUser admin = new YbUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("管理员");
            admin.setRole("admin");
            admin.setAvatarColor("#1a90ff");
            userRepository.save(admin);
        }
        if (!userRepository.existsByUsername("editor1")) {
            YbUser e1 = new YbUser();
            e1.setUsername("editor1");
            e1.setPassword(passwordEncoder.encode("123456"));
            e1.setName("张明");
            e1.setRole("editor");
            e1.setAvatarColor("#52c41a");
            userRepository.save(e1);
        }
        if (!userRepository.existsByUsername("editor2")) {
            YbUser e2 = new YbUser();
            e2.setUsername("editor2");
            e2.setPassword(passwordEncoder.encode("123456"));
            e2.setName("李华");
            e2.setRole("manager");
            e2.setAvatarColor("#fa8c16");
            userRepository.save(e2);
        }
        return userRepository.findByUsername("admin").orElseThrow();
    }
}
