package com.chronicle.controller;

import com.chronicle.entity.YbUser;
import com.chronicle.repository.YbUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final YbUserRepository userRepo;

    @GetMapping
    public List<YbUser> list() {
        List<YbUser> users = userRepo.findAll();
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @GetMapping("/{id}")
    public YbUser get(@PathVariable UUID id) {
        YbUser u = userRepo.findById(id).orElseThrow();
        u.setPassword(null);
        return u;
    }

    @PutMapping("/{id}")
    public YbUser update(@PathVariable UUID id, @RequestBody YbUser updates) {
        YbUser u = userRepo.findById(id).orElseThrow();
        if (updates.getName() != null) u.setName(updates.getName());
        if (updates.getPhone() != null) u.setPhone(updates.getPhone());
        if (updates.getEmail() != null) u.setEmail(updates.getEmail());
        YbUser saved = userRepo.save(u);
        saved.setPassword(null);
        return saved;
    }
}
