package com.chronicle.dto;

import com.chronicle.entity.YbUser;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private String id;
        private String username;
        private String name;
        private String role;
        private String avatarColor;
        private String phone;
        private String email;
    }

    public static LoginResponse of(String token, YbUser u) {
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        UserInfo info = new UserInfo();
        info.setId(u.getId().toString());
        info.setUsername(u.getUsername());
        info.setName(u.getName());
        info.setRole(u.getRole());
        info.setAvatarColor(u.getAvatarColor());
        info.setPhone(u.getPhone());
        info.setEmail(u.getEmail());
        resp.setUser(info);
        return resp;
    }
}
