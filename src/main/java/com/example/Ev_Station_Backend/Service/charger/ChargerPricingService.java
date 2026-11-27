package com.example.Ev_Station_Backend.Service.charger;

import com.example.Ev_Station_Backend.Repository.ChargerPricingRepository;
import com.example.Ev_Station_Backend.entity.ChargerPricing;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class ChargerPricingService {

    private final ChargerPricingRepository chargerPricingRepository;

    public ChargerPricingService(
            ChargerPricingRepository chargerPricingRepository) {

        this.chargerPricingRepository = chargerPricingRepository;
    }

    /**
     * Returns the configured price per kWh for a charger type.
     */
    public BigDecimal getPricePerKwh(String chargerType) {

        if (chargerType == null || chargerType.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Charger type is required"
            );
        }

        return chargerPricingRepository
                .findByChargerType(chargerType)
                .map(ChargerPricing::getPricePerKwh)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Pricing not configured for charger type: "
                                + chargerType
                ));
    }
}