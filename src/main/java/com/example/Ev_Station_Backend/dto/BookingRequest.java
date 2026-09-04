package com.example.Ev_Station_Backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingRequest {

    @NotNull(message = "Connector is required")
    private Long connectorId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}