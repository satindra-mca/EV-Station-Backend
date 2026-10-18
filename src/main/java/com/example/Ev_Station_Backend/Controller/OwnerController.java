package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.Service.ChargingStationService;
import com.example.Ev_Station_Backend.dto.StationResponse;
import com.example.Ev_Station_Backend.entity.ChargingStation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final ChargingStationService chargingStationService;

    public OwnerController(
            ChargingStationService chargingStationService) {
        this.chargingStationService = chargingStationService;
    }

   

    @GetMapping("/station")
    public ResponseEntity<StationResponse> getMyStation(
            Authentication authentication) {

        String email = authentication.getName();

        ChargingStation station =
                chargingStationService.getOwnerStation(email);

        StationResponse response = StationResponse.from(station);

        return ResponseEntity.ok(response);
    }

}
