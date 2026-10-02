package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.exception.BookingException;
import com.example.Ev_Station_Backend.Repository.BookingRepository;
import com.example.Ev_Station_Backend.Repository.UserRepository;
import com.example.Ev_Station_Backend.dto.BookingHistoryResponse;
import com.example.Ev_Station_Backend.entity.Booking;
import com.example.Ev_Station_Backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingHistoryService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingHistoryService(
            BookingRepository bookingRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    public List<BookingHistoryResponse> getMyBookings() {

        User currentUser = getCurrentUser();

        List<Booking> bookings =
                bookingRepository.findAllByUserIdOrderByCreatedAtDesc(
                        currentUser.getId()
                );

        return bookings.stream()
                .map(this::mapToBookingHistoryResponse)
                .toList();
    }

    public BookingHistoryResponse getBookingById(Long bookingId) {

        User currentUser = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingException("Booking not found")
                );

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new BookingException(
                    "You are not allowed to view this booking"
            );
        }

        return mapToBookingHistoryResponse(booking);
    }

    private BookingHistoryResponse mapToBookingHistoryResponse(
            Booking booking) {

        BookingHistoryResponse response =
                new BookingHistoryResponse();

        response.setBookingId(booking.getId());

        // Charging Station details
        response.setStationId(
                booking.getConnector()
                        .getCharger()
                        .getChargingStation()
                        .getId()
        );

        response.setCpoName(
                booking.getConnector()
                        .getCharger()
                        .getChargingStation()
                        .getCpoName()
        );

        response.setLocation(
                booking.getConnector()
                        .getCharger()
                        .getChargingStation()
                        .getLocation()
        );

        response.setCityVillage(
                booking.getConnector()
                        .getCharger()
                        .getChargingStation()
                        .getCityVillage()
        );

        // Charger details
        response.setChargerId(
                booking.getConnector()
                        .getCharger()
                        .getId()
        );

        response.setChargerType(
                booking.getConnector()
                        .getCharger()
                        .getChargerType()
        );

        // Connector details
        response.setConnectorId(
                booking.getConnector().getId()
        );

        response.setConnectorNumber(
                booking.getConnector().getConnectorNumber()
        );

        // Booking time
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());

        // Amount details
        response.setEstimatedAmount(
                booking.getEstimatedAmount()
        );

        response.setAdvanceAmount(
                booking.getAdvanceAmount()
        );

        response.setFinalAmount(
                booking.getFinalAmount()
        );

        // Status
        response.setStatus(booking.getStatus());
        response.setPaymentStatus(booking.getPaymentStatus());

        // Cancellation details
        response.setCancellationCharge(
                booking.getCancellationCharge()
        );

        response.setRefundAmount(
                booking.getRefundAmount()
        );

        // Timestamps
        response.setCreatedAt(booking.getCreatedAt());
        response.setCancelledAt(booking.getCancelledAt());

        return response;
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }
}