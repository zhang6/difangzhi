package com.chronicle.service;

import com.chronicle.entity.YbUser;
import com.chronicle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<YbUser> findAll() {
        return userRepository.findAll();
    }

    public YbUser findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public YbUser update(UUID id, YbUser updates) {
        YbUser user = findById(id);
        if (updates.getName() != null) user.setName(updates.getName());
        if (updates.getPhone() != null) user.setPhone(updates.getPhone());
        if (updates.getEmail() != null) user.setEmail(updates.getEmail());
        return userRepository.save(user);
    }
}
