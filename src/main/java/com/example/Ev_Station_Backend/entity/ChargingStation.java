package com.example.Ev_Station_Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "charging_stations")
@Getter
@Setter
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cpoName;

    private String govtPrivate;

    private String state;

    private String district;

    private String cityVillage;

    @Column(length = 500)
    private String location;

    private Double latitude;

    private Double longitude;

    private String source;

    private String googlePlaceId;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}