package com.example.Ev_Station_Backend.dto;

public class ChargingStationRequest {

    private String cpoName;
    private String govtPrivate;
    private String state;
    private String district;
    private String cityVillage;
    private String location;
    private Double latitude;
    private Double longitude;
    private String source;
    private String googlePlaceId;

    // Getters and Setters

    public String getCpoName() {
        return cpoName;
    }

    public void setCpoName(String cpoName) {
        this.cpoName = cpoName;
    }

    public String getGovtPrivate() {
        return govtPrivate;
    }

    public void setGovtPrivate(String govtPrivate) {
        this.govtPrivate = govtPrivate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCityVillage() {
        return cityVillage;
    }

    public void setCityVillage(String cityVillage) {
        this.cityVillage = cityVillage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getGooglePlaceId() {
        return googlePlaceId;
    }

    public void setGooglePlaceId(String googlePlaceId) {
        this.googlePlaceId = googlePlaceId;
    }
}