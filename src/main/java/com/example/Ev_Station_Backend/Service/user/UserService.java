package com.example.Ev_Station_Backend.Service.user;

import com.example.Ev_Station_Backend.entity.User;
import com.example.Ev_Station_Backend.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.example.Ev_Station_Backend.dto.user.UserCreateRequest;
import com.example.Ev_Station_Backend.dto.user.UserResponse;
import com.example.Ev_Station_Backend.Enum.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

        public User createUser(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered"
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.USER);
        user.setAuthProvider("LOCAL");

        return userRepository.save(user);
    }

            public List<User> getAllUsers() {
                return userRepository.findAll();
            }

    public List<UserResponse> getAllUserResponses() {

        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getMobile(),
                        user.getRole()
                ))
                .toList();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }




}