package com.example.Ev_Station_Backend.dto;
import com.example.Ev_Station_Backend.Enum.Role;



public class LoginResponse {

    private String message;
    private String token;
    private Long userId;
    private String name;
    private String email;
    private Role role;

    public LoginResponse(String message, String token, Long userId,
                         String name, String email, Role role) {
        this.message = message;
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}