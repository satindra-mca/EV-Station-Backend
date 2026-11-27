package com.example.Ev_Station_Backend.Controller.charger;

import com.example.Ev_Station_Backend.dto.charger.ChargerRequest;
import com.example.Ev_Station_Backend.dto.charger.ChargerResponse;
import com.example.Ev_Station_Backend.entity.Charger;
import com.example.Ev_Station_Backend.entity.ChargingStation;
import com.example.Ev_Station_Backend.Service.charger.ChargerService;
import com.example.Ev_Station_Backend.Service.station.ChargingStationService;
import com.example.Ev_Station_Backend.Service.charger.ChargerPricingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/chargers")
public class ChargerController {

        private final ChargerService chargerService;
        private final ChargingStationService chargingStationService;
        private final ChargerPricingService chargerPricingService;

        public ChargerController(
                ChargerService chargerService,
                ChargingStationService chargingStationService,
                ChargerPricingService chargerPricingService) {

        this.chargerService = chargerService;
        this.chargingStationService = chargingStationService;
        this.chargerPricingService = chargerPricingService;
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

        /**
         * Retrieves all chargers or chargers belonging to a specific station.
         */
        @GetMapping
        public ResponseEntity<List<ChargerResponse>> getAllChargers(

                @RequestParam(name = "stationId", required = false)
                @Positive(message = "Station ID must be greater than zero")
                Long stationId) {

        List<Charger> chargers;

        if (stationId != null) {

                // Validate that the requested station exists.
                chargingStationService.getStationById(stationId);

                chargers = chargerService.getChargersByStationId(
                        stationId
                );

        } else {

                chargers = chargerService.getAllChargers();
        }

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

        ChargerResponse response = new ChargerResponse();

        response.setId(charger.getId());
        response.setChargerType(charger.getChargerType());
        response.setChargerRating(charger.getChargerRating());
        response.setConnectorRating(charger.getConnectorRating());
        response.setNoOfConnector(charger.getNoOfConnector());

        response.setChargingStationId(
                charger.getChargingStation().getId()
        );

        response.setPricePerKwh(
        chargerPricingService.getPricePerKwh(
                charger.getChargerType()
                )
        );

        return response;
        }
}