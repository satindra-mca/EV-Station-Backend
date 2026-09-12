package com.example.Ev_Station_Backend.dto;

import com.example.Ev_Station_Backend.Enum.BookingStatus;
import com.example.Ev_Station_Backend.Enum.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BookingResponse {

    private Long id;

    private Long userId;

    private Long connectorId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal estimatedAmount;

    private BigDecimal advanceAmount;

    private BigDecimal finalAmount;

    private BookingStatus status;

    private PaymentStatus paymentStatus;

    private LocalDateTime paymentExpiresAt;

    private BigDecimal cancellationCharge;

    private BigDecimal refundAmount;

    private LocalDateTime createdAt;

    private LocalDateTime cancelledAt;
}