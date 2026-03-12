package com.chronicle.controller;

import com.chronicle.dto.LoginRequest;
import com.chronicle.dto.LoginResponse;
import com.chronicle.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/init")
    public String initUsers() {
        authService.initDefaultUsers();
        return "初始化完成";
    }
}
