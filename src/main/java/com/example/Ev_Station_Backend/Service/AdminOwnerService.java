package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.Enum.Role;
import com.example.Ev_Station_Backend.Repository.UserRepository;
import com.example.Ev_Station_Backend.dto.OwnerCreateRequest;
import com.example.Ev_Station_Backend.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminOwnerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminOwnerService(UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createOwner(OwnerCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User owner = new User();

        owner.setName(request.getName());
        owner.setEmail(request.getEmail());
        owner.setMobile(request.getMobile());

        // Plain password -> BCrypt hash
        owner.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Owner role is assigned by backend
        owner.setRole(Role.OWNER);

        // Owner created by Admin using local login
        owner.setAuthProvider("LOCAL");

        return userRepository.save(owner);
    }
}