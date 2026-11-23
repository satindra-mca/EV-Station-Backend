package com.example.Ev_Station_Backend.Controller.user;
import com.example.Ev_Station_Backend.entity.User;
import com.example.Ev_Station_Backend.Service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Ev_Station_Backend.dto.user.UserCreateRequest;
import com.example.Ev_Station_Backend.dto.user.UserResponse;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

        // Create User
        @PostMapping
        public ResponseEntity<UserResponse> createUser(
                @Valid @RequestBody UserCreateRequest request) {

            User savedUser = userService.createUser(request);

            UserResponse response = new UserResponse(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    savedUser.getMobile(),
                    savedUser.getRole()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        }

    // Get All Users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUserResponses());
    }


    // Get User By ID
    @GetMapping("/{id}")
        public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {

            return userService.getUserById(id)
                    .map(user -> new UserResponse(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getMobile(),
                            user.getRole()
                    ))
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }


    // Delete User
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}