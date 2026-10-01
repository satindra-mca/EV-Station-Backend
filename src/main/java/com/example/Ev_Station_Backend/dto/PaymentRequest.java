package com.example.Ev_Station_Backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "Payment amount is required")
    private BigDecimal amount;
}