
package com.example.Ev_Station_Backend.dto;

import com.example.Ev_Station_Backend.entity.ChargingStation;

public record StationResponse(
        Long id,
        String cpoName,
        String govtPrivate,
        String state,
        String district,
        String cityVillage,
        String location,
        Double latitude,
        Double longitude,
        String source,
        String googlePlaceId,
        String status
) {
    public static StationResponse from(ChargingStation station) {
        return new StationResponse(
                station.getId(),
                station.getCpoName(),
                station.getGovtPrivate(),
                station.getState(),
                station.getDistrict(),
                station.getCityVillage(),
                station.getLocation(),
                station.getLatitude(),
                station.getLongitude(),
                station.getSource(),
                station.getGooglePlaceId(),
                station.getStatus() == null
                        ? null
                        : station.getStatus().toString()
        );
    }
}
