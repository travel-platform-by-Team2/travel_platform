package com.example.travel_platform.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionUser {

    private Integer id;
    private String username;
    private String email;
    private String tel;
    private String role;

    public static SessionUser fromUser(User user) {
        return new SessionUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getTel(),
                user.getRole());
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
