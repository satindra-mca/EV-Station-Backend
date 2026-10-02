package com.example.Ev_Station_Backend.Controller;

import com.example.Ev_Station_Backend.Service.BookingService;
import com.example.Ev_Station_Backend.dto.BookingCancellationRequest;
import com.example.Ev_Station_Backend.dto.BookingRequest;
import com.example.Ev_Station_Backend.dto.BookingResponse;
import com.example.Ev_Station_Backend.dto.PaymentRequest;
import com.example.Ev_Station_Backend.entity.Booking;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Ev_Station_Backend.Service.BookingHistoryService;
import com.example.Ev_Station_Backend.dto.BookingHistoryResponse;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingHistoryService bookingHistoryService;

    public BookingController(BookingService bookingService , BookingHistoryService bookingHistoryService) {
        this.bookingService = bookingService;
        this.bookingHistoryService = bookingHistoryService;
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

                // Payment / Amount details
                response.setEstimatedAmount(
                        booking.getEstimatedAmount()
                );

                response.setAdvanceAmount(
                        booking.getAdvanceAmount()
                );

                response.setFinalAmount(
                        booking.getFinalAmount()
                );

                response.setStatus(
                        booking.getStatus()
                );

                response.setPaymentStatus(
                        booking.getPaymentStatus()
                );

                response.setPaymentExpiresAt(
                        booking.getPaymentExpiresAt()
                );

                response.setCancellationCharge(
                        booking.getCancellationCharge()
                );

                response.setRefundAmount(
                        booking.getRefundAmount()
                );

                response.setCreatedAt(
                        booking.getCreatedAt()
                );

                response.setCancelledAt(
                        booking.getCancelledAt()
                );

                return ResponseEntity.ok(response);
        }

       @PostMapping("/{bookingId}/payment")
                public ResponseEntity<BookingResponse> confirmPayment(
                        @PathVariable Long bookingId,
                        @Valid @RequestBody PaymentRequest request) {

                Booking booking = bookingService.confirmPayment(
                        bookingId,
                        request
                );

                BookingResponse response = new BookingResponse();

                response.setId(booking.getId());
                response.setUserId(booking.getUser().getId());
                response.setConnectorId(booking.getConnector().getId());

                response.setStartTime(booking.getStartTime());
                response.setEndTime(booking.getEndTime());

                response.setEstimatedAmount(
                        booking.getEstimatedAmount()
                );

                response.setAdvanceAmount(
                        booking.getAdvanceAmount()
                );

                response.setFinalAmount(
                        booking.getFinalAmount()
                );

                response.setStatus(
                        booking.getStatus()
                );

                response.setPaymentStatus(
                        booking.getPaymentStatus()
                );

                response.setPaymentExpiresAt(
                        booking.getPaymentExpiresAt()
                );

                response.setCancellationCharge(
                        booking.getCancellationCharge()
                );

                response.setRefundAmount(
                        booking.getRefundAmount()
                );

                response.setCreatedAt(
                        booking.getCreatedAt()
                );

                response.setCancelledAt(
                        booking.getCancelledAt()
                );

                return ResponseEntity.ok(response);
                }

        @PostMapping("/{bookingId}/cancel")
                public ResponseEntity<BookingResponse> cancelBooking(
                        @PathVariable Long bookingId,
                        @RequestBody BookingCancellationRequest request) {

                Booking booking = bookingService.cancelBooking(
                        bookingId,
                        request.getReason()
                );

                BookingResponse response = new BookingResponse();

                response.setId(booking.getId());
                response.setUserId(booking.getUser().getId());
                response.setConnectorId(booking.getConnector().getId());

                response.setStartTime(booking.getStartTime());
                response.setEndTime(booking.getEndTime());

                response.setEstimatedAmount(
                        booking.getEstimatedAmount()
                );

                response.setAdvanceAmount(
                        booking.getAdvanceAmount()
                );

                response.setFinalAmount(
                        booking.getFinalAmount()
                );

                response.setStatus(
                        booking.getStatus()
                );

                response.setPaymentStatus(
                        booking.getPaymentStatus()
                );

                response.setPaymentExpiresAt(
                        booking.getPaymentExpiresAt()
                );

                response.setCancellationCharge(
                        booking.getCancellationCharge()
                );

                response.setRefundAmount(
                        booking.getRefundAmount()
                );

                response.setCreatedAt(
                        booking.getCreatedAt()
                );

                response.setCancelledAt(
                        booking.getCancelledAt()
                );

                return ResponseEntity.ok(response);
                }

        @GetMapping("/my")
                public ResponseEntity<List<BookingHistoryResponse>> getMyBookings() {

                return ResponseEntity.ok(
                        bookingHistoryService.getMyBookings()
                );
                }

        @GetMapping("/{bookingId}")
                public ResponseEntity<BookingHistoryResponse> getBookingById(
                        @PathVariable Long bookingId) {

                return ResponseEntity.ok(
                        bookingHistoryService.getBookingById(bookingId)
                );
}
}

