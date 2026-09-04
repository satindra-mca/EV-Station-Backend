package com.example.Ev_Station_Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "charger_pricing",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "charger_type")
        }
)
@Getter
@Setter
public class ChargerPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "charger_type", nullable = false, unique = true)
    private String chargerType;

    @Column(name = "price_per_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKwh;
}