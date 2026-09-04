package com.example.Ev_Station_Backend.Repository;

import com.example.Ev_Station_Backend.entity.ChargerPricing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargerPricingRepository
        extends JpaRepository<ChargerPricing, Long> {

    Optional<ChargerPricing> findByChargerType(String chargerType);

    boolean existsByChargerType(String chargerType);
}