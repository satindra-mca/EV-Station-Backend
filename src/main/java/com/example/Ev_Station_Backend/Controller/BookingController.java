package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.Service.BookingService;
import com.example.Ev_Station_Backend.dto.BookingRequest;
import com.example.Ev_Station_Backend.dto.BookingResponse;
import com.example.Ev_Station_Backend.entity.Booking;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {

        Booking booking = bookingService.createBooking(request);

        BookingResponse response = new BookingResponse();

        response.setId(booking.getId());
        response.setUserId(booking.getUser().getId());
        response.setConnectorId(booking.getConnector().getId());
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setAmount(booking.getAmount());
        response.setStatus(booking.getStatus());
        response.setCancellationCharge(
                booking.getCancellationCharge()
        );
        response.setRefundAmount(
                booking.getRefundAmount()
        );
        response.setCreatedAt(booking.getCreatedAt());
        response.setCancelledAt(booking.getCancelledAt());

        return ResponseEntity.ok(response);
    }
}