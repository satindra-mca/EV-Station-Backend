package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.entity.ChargingStation;
import com.example.Ev_Station_Backend.Repository.ChargingStationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChargingStationService {

    private final ChargingStationRepository chargingStationRepository;

    public ChargingStationService(ChargingStationRepository chargingStationRepository) {
        this.chargingStationRepository = chargingStationRepository;
    }

    // Create / Update
    public ChargingStation saveStation(ChargingStation station) {
        return chargingStationRepository.save(station);
    }

    // Get all stations
    public List<ChargingStation> getAllStations() {
        return chargingStationRepository.findAll();
    }

    // Get station by ID
    public ChargingStation getStationById(Long id) {
        return chargingStationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charging station not found"));
    }

    // Delete station
    public void deleteStation(Long id) {
        chargingStationRepository.deleteById(id);
    }
}
