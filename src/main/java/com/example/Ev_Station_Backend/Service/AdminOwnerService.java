package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.Enum.Role;
import com.example.Ev_Station_Backend.Repository.UserRepository;
import com.example.Ev_Station_Backend.dto.OwnerCreateRequest;
import com.example.Ev_Station_Backend.entity.User;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.Ev_Station_Backend.Repository.ChargingStationRepository;
import com.example.Ev_Station_Backend.entity.ChargingStation;
import java.util.Optional;


@Service
public class AdminOwnerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChargingStationRepository chargingStationRepository;

    public AdminOwnerService(UserRepository userRepository,
                             PasswordEncoder passwordEncoder, ChargingStationRepository chargingStationRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.chargingStationRepository = chargingStationRepository;
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

    @Transactional
    public ChargingStation assignStationToOwner(Long ownerId, Long stationId) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (owner.getRole() != Role.OWNER) {
            throw new RuntimeException("User is not an OWNER");
        }

        ChargingStation station = chargingStationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        
        Optional<ChargingStation> existingStation =
                chargingStationRepository.findByOwner_Id(ownerId);

        // An owner can have only one station.
        if (existingStation.isPresent()
                && !existingStation.get().getId().equals(stationId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This owner already has another charging station"
            );
        }

        // A station already assigned to another owner cannot be reassigned.
        if (station.getOwner() != null
                && !station.getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This station is already assigned to another owner"
            );
        }

        station.setOwner(owner);
        return chargingStationRepository.save(station);
    }
}