package com.example.Ev_Station_Backend.dto.booking;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingCancellationRequest {

    private String reason;
}