package com.chronicle.dto;

import com.chronicle.entity.YbUser;
import lombok.Data;

import java.util.UUID;

@Data
public class LoginResponse {
    private String token;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private UUID id;
        private String username;
        private String name;
        private String role;
        private String avatarColor;
        private String phone;
        private String email;
    }

    public static LoginResponse of(String token, YbUser u) {
        LoginResponse r = new LoginResponse();
        r.setToken(token);
        UserInfo ui = new UserInfo();
        ui.setId(u.getId());
        ui.setUsername(u.getUsername());
        ui.setName(u.getName());
        ui.setRole(u.getRole());
        ui.setAvatarColor(u.getAvatarColor());
        ui.setPhone(u.getPhone());
        ui.setEmail(u.getEmail());
        r.setUser(ui);
        return r;
    }
}
