package com.example.Ev_Station_Backend.Repository;

import com.example.Ev_Station_Backend.entity.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {

    Optional<ChargingStation> findByCpoNameAndLocationAndLatitudeAndLongitude(
            String cpoName,
            String location,
            Double latitude,
            Double longitude
    );
}