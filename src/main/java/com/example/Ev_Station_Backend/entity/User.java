package com.example.Ev_Station_Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String mobile;

    @Column(name = "auth_provider")
    private String authProvider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(nullable = false)
    private String role;
}