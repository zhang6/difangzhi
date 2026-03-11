package com.chronicle.controller;

import com.chronicle.dto.LoginRequest;
import com.chronicle.dto.LoginResponse;
import com.chronicle.entity.YbUser;
import com.chronicle.repository.YbUserRepository;
import com.chronicle.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final YbUserRepository userRepo;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        String token = jwtService.generateToken((UserDetails) auth.getPrincipal());
        YbUser user = userRepo.findByUsername(req.getUsername()).orElseThrow();
        return LoginResponse.of(token, user);
    }
}
