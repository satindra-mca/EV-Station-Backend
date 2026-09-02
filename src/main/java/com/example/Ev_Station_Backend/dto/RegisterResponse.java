package com.example.Ev_Station_Backend.dto;

public class RegisterResponse {

    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String role;

    public RegisterResponse(Long id, String name, String email,
                            String mobile, String role) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getRole() {
        return role;
    }
}