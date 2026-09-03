package com.example.Ev_Station_Backend.Repository;

import com.example.Ev_Station_Backend.entity.Charger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargerRepository
        extends JpaRepository<Charger, Long> {

    Optional<Charger> findByBeeSourceIndex(Long beeSourceIndex);

}