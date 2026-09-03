package com.example.Ev_Station_Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chargers")
@Getter
@Setter
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chargerType;

    private String chargerRating;

    private String connectorRating;

    private Integer noOfConnector;

    /*
     * Unique source row number from BEE CSV.
     *
     * Example:
     * CSV row 1  -> beeSourceIndex = 1
     * CSV row 2  -> beeSourceIndex = 2
     * CSV row 3  -> beeSourceIndex = 3
     *
     * This is used to make BEE import idempotent.
     */
    @Column(name = "bee_source_index", unique = true)
    private Long beeSourceIndex;

    @ManyToOne
    @JoinColumn(name = "charging_station_id", nullable = false)
    private ChargingStation chargingStation;
}