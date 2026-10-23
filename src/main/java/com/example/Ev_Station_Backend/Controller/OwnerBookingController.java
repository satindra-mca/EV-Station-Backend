package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.Service.OwnerBookingService;
import com.example.Ev_Station_Backend.dto.BookingHistoryResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/owner/bookings")
public class OwnerBookingController {

    private final OwnerBookingService ownerBookingService;

    public OwnerBookingController(
            OwnerBookingService ownerBookingService) {
        this.ownerBookingService = ownerBookingService;
    }

    @GetMapping
    public List<BookingHistoryResponse> getAllBookings(
            Authentication authentication) {
        return ownerBookingService.getAllBookings(
                authentication.getName());
    }

    @GetMapping("/today")
    public List<BookingHistoryResponse> getTodayBookings(
            Authentication authentication) {
        return ownerBookingService.getTodayBookings(
                authentication.getName());
    }

    @GetMapping("/tomorrow")
    public List<BookingHistoryResponse> getTomorrowBookings(
            Authentication authentication) {
        return ownerBookingService.getTomorrowBookings(
                authentication.getName());
    }
}
