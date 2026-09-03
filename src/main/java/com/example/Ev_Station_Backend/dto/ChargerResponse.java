package com.example.Ev_Station_Backend.dto;

public class ChargerResponse {

    private Long id;
    private String chargerType;
    private String chargerRating;
    private String connectorRating;
    private Integer noOfConnector;
    private Long chargingStationId;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChargerType() {
        return chargerType;
    }

    public void setChargerType(String chargerType) {
        this.chargerType = chargerType;
    }

    public String getChargerRating() {
        return chargerRating;
    }

    public void setChargerRating(String chargerRating) {
        this.chargerRating = chargerRating;
    }

    public String getConnectorRating() {
        return connectorRating;
    }

    public void setConnectorRating(String connectorRating) {
        this.connectorRating = connectorRating;
    }

    public Integer getNoOfConnector() {
        return noOfConnector;
    }

    public void setNoOfConnector(Integer noOfConnector) {
        this.noOfConnector = noOfConnector;
    }

    public Long getChargingStationId() {
        return chargingStationId;
    }

    public void setChargingStationId(Long chargingStationId) {
        this.chargingStationId = chargingStationId;
    }
}