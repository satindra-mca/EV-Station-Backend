package com.example.Ev_Station_Backend.Repository;

import com.example.Ev_Station_Backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.connector.id = :connectorId
          AND b.status <> com.example.Ev_Station_Backend.Enum.BookingStatus.CANCELLED
          AND :startTime < b.endTime
          AND :endTime > b.startTime
    """)
    boolean existsOverlappingBooking(
            @Param("connectorId") Long connectorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}