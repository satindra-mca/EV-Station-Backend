package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.Service.AdminOwnerService;
import com.example.Ev_Station_Backend.dto.OwnerCreateRequest;
import com.example.Ev_Station_Backend.entity.User;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Ev_Station_Backend.entity.ChargingStation;


@RestController
@RequestMapping("/api/admin/owners")
public class AdminOwnerController {

    private final AdminOwnerService adminOwnerService;

    public AdminOwnerController(AdminOwnerService adminOwnerService) {
        this.adminOwnerService = adminOwnerService;
    }

    @PostMapping
    public ResponseEntity<OwnerResponse> createOwner(
            @Valid @RequestBody OwnerCreateRequest request) {

        User owner = adminOwnerService.createOwner(request);

        OwnerResponse response = new OwnerResponse(
                owner.getId(),
                owner.getName(),
                owner.getEmail(),
                owner.getMobile(),
                owner.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{ownerId}/station/{stationId}")
        public ResponseEntity<String> assignStationToOwner(
                @PathVariable Long ownerId,
                @PathVariable Long stationId) {

        adminOwnerService.assignStationToOwner(ownerId, stationId);

        return ResponseEntity.ok(
                "Charging station assigned to owner successfully"
        );
        }

    public record OwnerResponse(
            Long id,
            String name,
            String email,
            String mobile,
            com.example.Ev_Station_Backend.Enum.Role role
    ) {}
}
