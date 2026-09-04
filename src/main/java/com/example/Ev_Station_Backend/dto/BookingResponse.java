package com.example.Ev_Station_Backend.dto;

import com.example.Ev_Station_Backend.Enum.BookingStatus;
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

    private BigDecimal amount;

    private BookingStatus status;

    private BigDecimal cancellationCharge;

    private BigDecimal refundAmount;

    private LocalDateTime createdAt;

    private LocalDateTime cancelledAt;
}