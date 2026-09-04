package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.dto.ChargingStationRequest;
import com.example.Ev_Station_Backend.dto.ChargingStationResponse;
import com.example.Ev_Station_Backend.entity.ChargingStation;
import com.example.Ev_Station_Backend.Service.ChargingStationService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stations")
public class ChargingStationController {

    private final ChargingStationService chargingStationService;

    public ChargingStationController(ChargingStationService chargingStationService) {
        this.chargingStationService = chargingStationService;
    }

    // Create Station
    @PostMapping
    public ResponseEntity<ChargingStationResponse> createStation(
            @RequestBody ChargingStationRequest request) {

        ChargingStation station = new ChargingStation();

        station.setCpoName(request.getCpoName());
        station.setGovtPrivate(request.getGovtPrivate());
        station.setState(request.getState());
        station.setDistrict(request.getDistrict());
        station.setCityVillage(request.getCityVillage());
        station.setLocation(request.getLocation());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setSource(request.getSource());
        station.setGooglePlaceId(request.getGooglePlaceId());
        station.setStatus("ACTIVE");

        ChargingStation savedStation =
                chargingStationService.saveStation(station);

        return new ResponseEntity<>(
                convertToResponse(savedStation),
                HttpStatus.CREATED
        );
    }

    // Get All Stations
    @GetMapping
    public ResponseEntity<List<ChargingStationResponse>> getAllStations() {

        List<ChargingStation> stations =
                chargingStationService.getAllStations();

        List<ChargingStationResponse> response = stations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Get Station By ID
    @GetMapping("/{id}")
    public ResponseEntity<ChargingStationResponse> getStationById(
            @PathVariable Long id) {

        ChargingStation station =
                chargingStationService.getStationById(id);

        return ResponseEntity.ok(convertToResponse(station));
    }

    // Get Nearby Charging Stations
    @GetMapping("/nearby")
    public ResponseEntity<List<ChargingStationResponse>> getNearbyStations(
            @RequestParam
            @DecimalMin(
                    value = "-90.0",
                    message = "Latitude must be between -90 and 90"
            )
            @DecimalMax(
                    value = "90.0",
                    message = "Latitude must be between -90 and 90"
            )
            Double latitude,

            @RequestParam
            @DecimalMin(
                    value = "-180.0",
                    message = "Longitude must be between -180 and 180"
            )
            @DecimalMax(
                    value = "180.0",
                    message = "Longitude must be between -180 and 180"
            )
            Double longitude,

            @RequestParam
            @Positive(
                    message = "Radius must be greater than 0"
            )
            @DecimalMax(
                    value = "100.0",
                    message = "Radius must not exceed 100 km"
            )
            Double radius) {

        List<ChargingStation> stations =
                chargingStationService.getNearbyStations(
                        latitude,
                        longitude,
                        radius
                );

        List<ChargingStationResponse> response = stations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Delete Station
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(
            @PathVariable Long id) {

        chargingStationService.deleteStation(id);

        return ResponseEntity.noContent().build();
    }

    // Entity → Response DTO
    private ChargingStationResponse convertToResponse(
            ChargingStation station) {

        ChargingStationResponse response =
                new ChargingStationResponse();

        response.setId(station.getId());
        response.setCpoName(station.getCpoName());
        response.setGovtPrivate(station.getGovtPrivate());
        response.setState(station.getState());
        response.setDistrict(station.getDistrict());
        response.setCityVillage(station.getCityVillage());
        response.setLocation(station.getLocation());
        response.setLatitude(station.getLatitude());
        response.setLongitude(station.getLongitude());
        response.setSource(station.getSource());
        response.setGooglePlaceId(station.getGooglePlaceId());
        response.setStatus(station.getStatus());

        return response;
    }
}

