package com.chronicle.controller;

import com.chronicle.entity.YbUser;
import com.chronicle.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<YbUser> list() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public YbUser get(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public YbUser update(@PathVariable UUID id, @RequestBody YbUser updates) {
        return userService.update(id, updates);
    }
}
