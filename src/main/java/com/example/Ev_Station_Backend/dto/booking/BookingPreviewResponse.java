
package com.example.Ev_Station_Backend.dto.booking;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents slot availability and estimated booking
 * charges without creating a booking.
 */
@Getter
@Setter
public class BookingPreviewResponse {

    private Long connectorId;

    private boolean available;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private long durationMinutes;

    private BigDecimal pricePerKwh;

    private BigDecimal estimatedEnergy;

    private BigDecimal estimatedAmount;

    private BigDecimal advanceAmount;

    private String message;
}
