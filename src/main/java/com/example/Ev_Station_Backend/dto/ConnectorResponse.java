package com.example.Ev_Station_Backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectorResponse {

    private Long id;
    private Integer connectorNumber;
    private String status;
    private Long chargerId;
}