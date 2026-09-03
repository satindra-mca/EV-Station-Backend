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

    @ManyToOne
    @JoinColumn(name = "charging_station_id", nullable = false)
    private ChargingStation chargingStation;
}