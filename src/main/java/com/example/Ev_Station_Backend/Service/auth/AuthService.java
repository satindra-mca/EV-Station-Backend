package com.example.Ev_Station_Backend.Service.auth;
import com.example.Ev_Station_Backend.Security.JwtService;
import com.example.Ev_Station_Backend.dto.auth.RegisterRequest;
import com.example.Ev_Station_Backend.entity.User;
import com.example.Ev_Station_Backend.Enum.Role;
import com.example.Ev_Station_Backend.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.Ev_Station_Backend.dto.auth.LoginRequest;
import com.example.Ev_Station_Backend.dto.auth.LoginResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email already registered"
        );
}

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Plain password -> BCrypt hash
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setMobile(request.getMobile());

        // Default role
        user.setRole(Role.USER);

        // Normal registration
        user.setAuthProvider("LOCAL");

        userRepository.save(user);

        return user;
    }

    public LoginResponse login(LoginRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                    new BadCredentialsException(
                            "Invalid email or password"
                    )
            );

    if (!passwordEncoder.matches(
            request.getPassword(),
            user.getPassword())) {

        throw new BadCredentialsException(
                "Invalid email or password"
        );
    }

    String token = jwtService.generateToken(
            user.getEmail(),
            user.getRole()
    );

    return new LoginResponse(
            "Login successful",
            token,
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole()
    );
}
}