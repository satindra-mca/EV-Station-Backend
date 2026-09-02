package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.dto.RegisterRequest;
import com.example.Ev_Station_Backend.dto.RegisterResponse;
import com.example.Ev_Station_Backend.entity.User;
import com.example.Ev_Station_Backend.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Ev_Station_Backend.dto.LoginRequest;
import com.example.Ev_Station_Backend.dto.LoginResponse;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobile(),
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @RequestBody LoginRequest request) {

    LoginResponse response = authService.login(request);

    return ResponseEntity.ok(response);
}
}