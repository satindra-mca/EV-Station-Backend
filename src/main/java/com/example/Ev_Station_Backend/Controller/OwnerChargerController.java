package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.Service.OwnerChargerService;
import com.example.Ev_Station_Backend.dto.ChargerRequest;
import com.example.Ev_Station_Backend.dto.ChargerResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/chargers")
public class OwnerChargerController {

    private final OwnerChargerService ownerChargerService;

    public OwnerChargerController(
            OwnerChargerService ownerChargerService) {
        this.ownerChargerService = ownerChargerService;
    }

    // Get all chargers belonging to the logged-in owner's station
    @GetMapping
    public ResponseEntity<List<ChargerResponse>> getOwnerChargers(
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerChargerService.getOwnerChargers(
                        authentication.getName()
                )
        );
    }

    // Get a specific charger
    @GetMapping("/{id}")
    public ResponseEntity<ChargerResponse> getOwnerChargerById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerChargerService.getOwnerChargerById(
                        authentication.getName(), id
                )
        );
    }

    // Create a charger in the owner's assigned station
    @PostMapping
    public ResponseEntity<ChargerResponse> createCharger(
            @Valid @RequestBody ChargerRequest request,
            Authentication authentication) {

        ChargerResponse response =
                ownerChargerService.createCharger(
                        authentication.getName(), request
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // Update an existing charger
    @PutMapping("/{id}")
    public ResponseEntity<ChargerResponse> updateCharger(
            @PathVariable Long id,
            @Valid @RequestBody ChargerRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerChargerService.updateCharger(
                        authentication.getName(), id, request
                )
        );
    }

    // Delete a charger if no booking records exist
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharger(
            @PathVariable Long id,
            Authentication authentication) {

        ownerChargerService.deleteCharger(
                authentication.getName(), id
        );

        return ResponseEntity.noContent().build();
    }
}