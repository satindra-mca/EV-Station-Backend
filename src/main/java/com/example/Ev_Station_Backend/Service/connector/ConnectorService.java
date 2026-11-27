package com.example.Ev_Station_Backend.Service.connector;

import com.example.Ev_Station_Backend.Repository.ConnectorRepository;
import com.example.Ev_Station_Backend.entity.Connector;
import org.springframework.stereotype.Service;
import com.example.Ev_Station_Backend.exception.ResourceNotFoundException;
import java.util.List;

@Service
public class ConnectorService {

    private final ConnectorRepository connectorRepository;

    public ConnectorService(ConnectorRepository connectorRepository) {
        this.connectorRepository = connectorRepository;
    }

    // Create / Update
    public Connector saveConnector(Connector connector) {
        return connectorRepository.save(connector);
    }

    // Get all connectors
    public List<Connector> getAllConnectors() {
        return connectorRepository.findAll();
    }

    /**
     * Retrieves connectors belonging to the specified charger.
     */
    public List<Connector> getConnectorsByChargerId(Long chargerId) {

        return connectorRepository.findAllByChargerId(
                chargerId
        );
    }

    // Get connector by ID
    public Connector getConnectorById(Long id) {
        return connectorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Connector not found"));
    }

    // Delete connector
    public void deleteConnector(Long id) {
        connectorRepository.deleteById(id);
    }
}