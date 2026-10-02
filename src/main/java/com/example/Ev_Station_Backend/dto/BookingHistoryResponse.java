package com.example.Ev_Station_Backend.dto;

import com.example.Ev_Station_Backend.Enum.BookingStatus;
import com.example.Ev_Station_Backend.Enum.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BookingHistoryResponse {

    private Long bookingId;

    // Charging Station details
    private Long stationId;
    private String cpoName;
    private String location;
    private String cityVillage;

    // Charger details
    private Long chargerId;
    private String chargerType;

    // Connector details
    private Long connectorId;
    private Integer connectorNumber;

    // Booking time
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Payment / Amount details
    private BigDecimal estimatedAmount;
    private BigDecimal advanceAmount;
    private BigDecimal finalAmount;

    // Booking / Payment status
    private BookingStatus status;
    private PaymentStatus paymentStatus;

    // Cancellation details
    private BigDecimal cancellationCharge;
    private BigDecimal refundAmount;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
}