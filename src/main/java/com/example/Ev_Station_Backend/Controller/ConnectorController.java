package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.Service.ConnectorService;
import com.example.Ev_Station_Backend.dto.ConnectorRequest;
import com.example.Ev_Station_Backend.dto.ConnectorResponse;
import com.example.Ev_Station_Backend.entity.Charger;
import com.example.Ev_Station_Backend.entity.Connector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/connectors")
public class ConnectorController {

    private final ConnectorService connectorService;

    public ConnectorController(ConnectorService connectorService) {
        this.connectorService = connectorService;
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

    @GetMapping
    public ResponseEntity<List<ConnectorResponse>> getAllConnectors() {

        List<ConnectorResponse> response =
                connectorService.getAllConnectors()
                        .stream()
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