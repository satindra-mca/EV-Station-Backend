package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.dto.ChargerRequest;
import com.example.Ev_Station_Backend.dto.ChargerResponse;
import com.example.Ev_Station_Backend.entity.Charger;
import com.example.Ev_Station_Backend.entity.ChargingStation;
import com.example.Ev_Station_Backend.Service.ChargerService;
import com.example.Ev_Station_Backend.Service.ChargingStationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chargers")
public class ChargerController {

    private final ChargerService chargerService;
    private final ChargingStationService chargingStationService;

    public ChargerController(
            ChargerService chargerService,
            ChargingStationService chargingStationService) {

        this.chargerService = chargerService;
        this.chargingStationService = chargingStationService;
    }

    // Create Charger
    @PostMapping
    public ResponseEntity<ChargerResponse> createCharger(
            @RequestBody ChargerRequest request) {

        ChargingStation station =
                chargingStationService.getStationById(
                        request.getChargingStationId());

        Charger charger = new Charger();

        charger.setChargerType(request.getChargerType());
        charger.setChargerRating(request.getChargerRating());
        charger.setConnectorRating(request.getConnectorRating());
        charger.setNoOfConnector(request.getNoOfConnector());
        charger.setChargingStation(station);

        Charger savedCharger =
                chargerService.saveCharger(charger);

        return new ResponseEntity<>(
                convertToResponse(savedCharger),
                HttpStatus.CREATED
        );
    }

    // Get All Chargers
    @GetMapping
    public ResponseEntity<List<ChargerResponse>> getAllChargers() {

        List<Charger> chargers =
                chargerService.getAllChargers();

        List<ChargerResponse> response = chargers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Get Charger By ID
    @GetMapping("/{id}")
    public ResponseEntity<ChargerResponse> getChargerById(
            @PathVariable Long id) {

        Charger charger =
                chargerService.getChargerById(id);

        return ResponseEntity.ok(convertToResponse(charger));
    }

    // Delete Charger
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharger(
            @PathVariable Long id) {

        chargerService.deleteCharger(id);

        return ResponseEntity.noContent().build();
    }

    // Entity → Response DTO
    private ChargerResponse convertToResponse(Charger charger) {

        ChargerResponse response =
                new ChargerResponse();

        response.setId(charger.getId());
        response.setChargerType(charger.getChargerType());
        response.setChargerRating(charger.getChargerRating());
        response.setConnectorRating(charger.getConnectorRating());
        response.setNoOfConnector(charger.getNoOfConnector());
        response.setChargingStationId(
                charger.getChargingStation().getId());

        return response;
    }
}