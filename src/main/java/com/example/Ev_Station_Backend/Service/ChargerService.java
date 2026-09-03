package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.entity.Charger;
import com.example.Ev_Station_Backend.Repository.ChargerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChargerService {

    private final ChargerRepository chargerRepository;

    public ChargerService(ChargerRepository chargerRepository) {
        this.chargerRepository = chargerRepository;
    }

    // Create / Update
    public Charger saveCharger(Charger charger) {
        return chargerRepository.save(charger);
    }

    // Get all chargers
    public List<Charger> getAllChargers() {
        return chargerRepository.findAll();
    }

    // Get charger by ID
    public Charger getChargerById(Long id) {
        return chargerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charger not found"));
    }

    // Delete charger
    public void deleteCharger(Long id) {
        chargerRepository.deleteById(id);
    }
}
