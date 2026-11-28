package com.example.Ev_Station_Backend.dto.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingRequest {

    @NotNull(message = "Connector is required")
    @Positive(message = "Connector ID must be greater than zero")
    private Long connectorId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}
