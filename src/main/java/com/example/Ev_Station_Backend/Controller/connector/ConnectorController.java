package com.example.Ev_Station_Backend.Controller.connector;

import com.example.Ev_Station_Backend.Service.connector.ConnectorService;
import com.example.Ev_Station_Backend.dto.connector.ConnectorRequest;
import com.example.Ev_Station_Backend.dto.connector.ConnectorResponse;
import com.example.Ev_Station_Backend.entity.Charger;
import com.example.Ev_Station_Backend.entity.Connector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import com.example.Ev_Station_Backend.Service.charger.ChargerService;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/connectors")
public class ConnectorController {

        private final ConnectorService connectorService;
        private final ChargerService chargerService;

        public ConnectorController(
                ConnectorService connectorService,
                ChargerService chargerService) {

        this.connectorService = connectorService;
        this.chargerService = chargerService;
        }

    @PostMapping
    public ResponseEntity<ConnectorResponse> createConnector(
            @RequestBody ConnectorRequest request) {

        Connector connector = new Connector();

        connector.setConnectorNumber(
                request.getConnectorNumber()
        );

        connector.setStatus(
                request.getStatus()
        );

        Charger charger = new Charger();
        charger.setId(request.getChargerId());

        connector.setCharger(charger);

        Connector savedConnector =
                connectorService.saveConnector(connector);

        return new ResponseEntity<>(
                convertToResponse(savedConnector),
                HttpStatus.CREATED
        );
    }

        /**
         * Retrieves all connectors or connectors belonging to a specific charger.
         */
        @GetMapping
        public ResponseEntity<List<ConnectorResponse>> getAllConnectors(

                @RequestParam(name = "chargerId", required = false)
                @Positive(message = "Charger ID must be greater than zero")
                Long chargerId) {

        List<Connector> connectors;

        if (chargerId != null) {

                // Validate that the requested charger exists.
                chargerService.getChargerById(chargerId);

                connectors = connectorService.getConnectorsByChargerId(
                        chargerId
                );

        } else {

                connectors = connectorService.getAllConnectors();
        }

        List<ConnectorResponse> response = connectors.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
        }

    @GetMapping("/{id}")
    public ResponseEntity<ConnectorResponse> getConnectorById(
            @PathVariable Long id) {

        Connector connector =
                connectorService.getConnectorById(id);

        return ResponseEntity.ok(
                convertToResponse(connector)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnector(
            @PathVariable Long id) {

        connectorService.deleteConnector(id);

        return ResponseEntity.noContent().build();
    }

    private ConnectorResponse convertToResponse(
            Connector connector) {

        ConnectorResponse response =
                new ConnectorResponse();

        response.setId(connector.getId());

        response.setConnectorNumber(
                connector.getConnectorNumber()
        );

        response.setStatus(
                connector.getStatus()
        );

        if (connector.getCharger() != null) {
            response.setChargerId(
                    connector.getCharger().getId()
            );
        }

        return response;
    }
}