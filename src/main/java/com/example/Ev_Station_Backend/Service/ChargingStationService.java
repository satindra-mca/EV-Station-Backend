package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.entity.ChargingStation;
import com.example.Ev_Station_Backend.Repository.ChargingStationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.Ev_Station_Backend.Repository.UserRepository;
import com.example.Ev_Station_Backend.entity.User;
import com.example.Ev_Station_Backend.Enum.Role;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
public class ChargingStationService {

    private final ChargingStationRepository chargingStationRepository;
    private final UserRepository userRepository;

    public ChargingStationService(ChargingStationRepository chargingStationRepository , UserRepository userRepository) {
        this.chargingStationRepository = chargingStationRepository;
        this.userRepository = userRepository;
    }

    // Create / Update
    public ChargingStation saveStation(ChargingStation station) {
        return chargingStationRepository.save(station);
    }

    // Get all stations
    public List<ChargingStation> getAllStations() {
        return chargingStationRepository.findAll();
    }

    // Get station by ID
    public ChargingStation getStationById(Long id) {
        return chargingStationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charging station not found"));
    }

    // Delete station
    public void deleteStation(Long id) {
        chargingStationRepository.deleteById(id);
    }

    // Get nearby charging stations
    public List<ChargingStation> getNearbyStations(
            Double latitude,
            Double longitude,
            Double radius) {

        return chargingStationRepository.findNearbyStations(
                latitude,
                longitude,
                radius
        );
    }

    public ChargingStation getOwnerStation(String email) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (owner.getRole() != Role.OWNER) {
            throw new AccessDeniedException(
                    "Only owners can access this resource");
        }

        return chargingStationRepository.findByOwner_Id(owner.getId())
        .orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No charging station assigned to this owner"));
    }
}