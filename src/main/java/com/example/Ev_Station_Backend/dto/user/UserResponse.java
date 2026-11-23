package com.example.Ev_Station_Backend.dto.user;

import com.example.Ev_Station_Backend.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String mobile;
    private Role role;
}
