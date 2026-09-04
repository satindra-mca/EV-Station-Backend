package com.example.Ev_Station_Backend.Repository;

import com.example.Ev_Station_Backend.entity.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {

    Optional<ChargingStation> findByCpoNameAndLocationAndLatitudeAndLongitude(
            String cpoName,
            String location,
            Double latitude,
            Double longitude
    );

    @Query(value = """
            SELECT *
            FROM charging_stations
            WHERE latitude IS NOT NULL
              AND longitude IS NOT NULL
              AND (
                  6371 * 2 * ASIN(
                      SQRT(
                          POWER(SIN(RADIANS(latitude - :latitude) / 2), 2)
                          +
                          COS(RADIANS(:latitude))
                          * COS(RADIANS(latitude))
                          * POWER(SIN(RADIANS(longitude - :longitude) / 2), 2)
                      )
                  )
              ) <= :radius
            """, nativeQuery = true)
    List<ChargingStation> findNearbyStations(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radius") Double radius
    );
}