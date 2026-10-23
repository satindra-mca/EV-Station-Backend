package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.Repository.BookingRepository;
import com.example.Ev_Station_Backend.dto.BookingHistoryResponse;
import com.example.Ev_Station_Backend.entity.Booking;
import com.example.Ev_Station_Backend.entity.ChargingStation;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OwnerBookingService {

    private final BookingRepository bookingRepository;
    private final ChargingStationService chargingStationService;

    public OwnerBookingService(
            BookingRepository bookingRepository,
            ChargingStationService chargingStationService) {
        this.bookingRepository = bookingRepository;
        this.chargingStationService = chargingStationService;
    }

    public List<BookingHistoryResponse> getAllBookings(String email) {
        ChargingStation station =
                chargingStationService.getOwnerStation(email);

        List<Booking> bookings =
                bookingRepository
                        .findAllByConnector_Charger_ChargingStation_IdOrderByStartTimeAsc(
                                station.getId());

        return mapBookings(bookings);
    }

    public List<BookingHistoryResponse> getTodayBookings(String email) {
        return getBookingsForDate(email, LocalDate.now());
    }

    public List<BookingHistoryResponse> getTomorrowBookings(String email) {
        return getBookingsForDate(email, LocalDate.now().plusDays(1));
    }

    private List<BookingHistoryResponse> getBookingsForDate(
            String email, LocalDate date) {

        ChargingStation station =
                chargingStationService.getOwnerStation(email);

        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();

        List<Booking> bookings =
                bookingRepository
                        .findAllByConnector_Charger_ChargingStation_IdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
                                station.getId(), startTime, endTime);

        return mapBookings(bookings);
    }

    private List<BookingHistoryResponse> mapBookings(
            List<Booking> bookings) {

        return bookings.stream().map(booking -> {
            BookingHistoryResponse response =
                    new BookingHistoryResponse();

            var connector = booking.getConnector();
            var charger = connector.getCharger();
            var station = charger.getChargingStation();

            response.setBookingId(booking.getId());

            response.setStationId(station.getId());
            response.setCpoName(station.getCpoName());
            response.setLocation(station.getLocation());
            response.setCityVillage(station.getCityVillage());

            response.setChargerId(charger.getId());
            response.setChargerType(charger.getChargerType());

            response.setConnectorId(connector.getId());
            response.setConnectorNumber(
                    connector.getConnectorNumber());

            response.setStartTime(booking.getStartTime());
            response.setEndTime(booking.getEndTime());

            response.setEstimatedAmount(booking.getEstimatedAmount());
            response.setAdvanceAmount(booking.getAdvanceAmount());
            response.setFinalAmount(booking.getFinalAmount());

            response.setStatus(booking.getStatus());
            response.setPaymentStatus(booking.getPaymentStatus());

            response.setCancellationCharge(
                    booking.getCancellationCharge());
            response.setRefundAmount(booking.getRefundAmount());

            response.setCreatedAt(booking.getCreatedAt());
            response.setCancelledAt(booking.getCancelledAt());

            return response;
        }).toList();
    }
}
