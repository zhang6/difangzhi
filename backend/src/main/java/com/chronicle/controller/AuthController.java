package com.chronicle.controller;

import com.chronicle.dto.AuthRequest;
import com.chronicle.dto.AuthResponse;
import com.chronicle.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        String token = jwtService.generateToken(
            (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal());
        return new AuthResponse(token, request.getUsername(),
            auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
    }
}
