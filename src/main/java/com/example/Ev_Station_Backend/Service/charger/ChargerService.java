package com.example.Ev_Station_Backend.Service.charger;

import com.example.Ev_Station_Backend.entity.Charger;
import com.example.Ev_Station_Backend.Repository.ChargerRepository;
import com.example.Ev_Station_Backend.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChargerService {

    private final ChargerRepository chargerRepository;
    private final ChargerPricingService chargerPricingService;

    public ChargerService(
            ChargerRepository chargerRepository,
            ChargerPricingService chargerPricingService) {

        this.chargerRepository = chargerRepository;
        this.chargerPricingService = chargerPricingService;
    }

    // Create or update charger with configured pricing
    public Charger saveCharger(Charger charger) {

        charger.setPricePerKwh(
                chargerPricingService.getPricePerKwh(
                        charger.getChargerType()
                )
        );

        return chargerRepository.save(charger);
    }

    // Retrieve all chargers
    public List<Charger> getAllChargers() {
        return chargerRepository.findAll();
    }

    // Retrieve chargers belonging to a specific station
    public List<Charger> getChargersByStationId(Long stationId) {
        return chargerRepository.findAllByChargingStationId(stationId);
    }

    // Retrieve charger by ID
    public Charger getChargerById(Long id) {
        return chargerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Charger not found with ID: " + id
                        )
                );
    }

    // Delete charger
    public void deleteCharger(Long id) {
        chargerRepository.deleteById(id);
    }
}