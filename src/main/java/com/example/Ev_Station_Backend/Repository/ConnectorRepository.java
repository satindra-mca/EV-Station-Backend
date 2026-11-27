package com.example.Ev_Station_Backend.Repository;

import com.example.Ev_Station_Backend.entity.Connector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConnectorRepository extends JpaRepository<Connector, Long> {

    Optional<Connector> findByChargerIdAndConnectorNumber(
            Long chargerId,
            Integer connectorNumber
    );

    List<Connector> findAllByChargerChargingStationId(Long stationId);
    List<Connector> findAllByChargerId(Long chargerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Connector c WHERE c.id = :connectorId")
    Optional<Connector> findByIdForUpdate(
            @Param("connectorId") Long connectorId
);
}