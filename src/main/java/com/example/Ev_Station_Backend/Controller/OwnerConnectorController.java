
package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.Service.OwnerConnectorService;
import com.example.Ev_Station_Backend.dto.ConnectorRequest;
import com.example.Ev_Station_Backend.dto.OwnerConnectorResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/connectors")
public class OwnerConnectorController {

    private final OwnerConnectorService ownerConnectorService;

    public OwnerConnectorController(
            OwnerConnectorService ownerConnectorService) {
        this.ownerConnectorService = ownerConnectorService;
    }

    @GetMapping
    public ResponseEntity<List<OwnerConnectorResponse>> getOwnerConnectors(
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerConnectorService.getOwnerConnectors(
                        authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OwnerConnectorResponse> getOwnerConnectorById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerConnectorService.getOwnerConnectorById(
                        authentication.getName(), id));
    }

    @PostMapping
    public ResponseEntity<OwnerConnectorResponse> createConnector(
            @Valid @RequestBody ConnectorRequest request,
            Authentication authentication) {

        OwnerConnectorResponse response =
                ownerConnectorService.createConnector(
                        authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OwnerConnectorResponse> updateConnector(
            @PathVariable Long id,
            @Valid @RequestBody ConnectorRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerConnectorService.updateConnector(
                        authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnector(
            @PathVariable Long id,
            Authentication authentication) {

        ownerConnectorService.deleteConnector(
                authentication.getName(), id);

        return ResponseEntity.noContent().build();
    }
}
