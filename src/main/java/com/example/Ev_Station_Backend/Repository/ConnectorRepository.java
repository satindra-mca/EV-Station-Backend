package com.example.Ev_Station_Backend.Repository;

import com.example.Ev_Station_Backend.entity.Connector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConnectorRepository extends JpaRepository<Connector, Long> {

    Optional<Connector> findByChargerIdAndConnectorNumber(
            Long chargerId,
            Integer connectorNumber
    );
}