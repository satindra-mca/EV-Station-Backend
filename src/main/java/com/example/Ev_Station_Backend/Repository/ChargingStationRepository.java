package com.example.Ev_Station_Backend.Repository;

import com.example.Ev_Station_Backend.entity.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargingStationRepository
        extends JpaRepository<ChargingStation, Long> {
}
