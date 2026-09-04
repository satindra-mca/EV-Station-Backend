package com.example.Ev_Station_Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "connectors",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"charger_id", "connector_number"}
        )
    }
)
@Getter
@Setter
public class Connector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "connector_number", nullable = false)
    private Integer connectorNumber;

    private String status;

    @ManyToOne
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;
}