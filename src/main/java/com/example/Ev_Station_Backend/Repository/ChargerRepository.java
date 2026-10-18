package com.example.Ev_Station_Backend.Repository;
import com.example.Ev_Station_Backend.entity.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ChargerRepository
        extends JpaRepository<Charger, Long> {

    Optional<Charger> findByBeeSourceIndex(Long beeSourceIndex);
    List<Charger> findAllByChargingStationId(Long stationId);

}