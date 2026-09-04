package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.Enum.BookingStatus;
import com.example.Ev_Station_Backend.Repository.BookingRepository;
import com.example.Ev_Station_Backend.Repository.ChargerPricingRepository;
import com.example.Ev_Station_Backend.Repository.ConnectorRepository;
import com.example.Ev_Station_Backend.Repository.UserRepository;
import com.example.Ev_Station_Backend.dto.BookingRequest;
import com.example.Ev_Station_Backend.entity.Booking;
import com.example.Ev_Station_Backend.entity.ChargerPricing;
import com.example.Ev_Station_Backend.entity.Connector;
import com.example.Ev_Station_Backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private static final long BOOKING_BUFFER_MINUTES = 15;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ConnectorRepository connectorRepository;
    private final ChargerPricingRepository chargerPricingRepository;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ConnectorRepository connectorRepository,
            ChargerPricingRepository chargerPricingRepository) {

        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.connectorRepository = connectorRepository;
        this.chargerPricingRepository = chargerPricingRepository;
    }

    /*
     * Get currently logged-in user from JWT
     */
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                authentication.getName() == null) {

            throw new RuntimeException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    /*
     * Create Booking
     */
    public Booking createBooking(BookingRequest request) {

        // 1. Get logged-in user
        User user = getCurrentUser();

        // 2. Check connector exists
        Connector connector = connectorRepository.findById(
                request.getConnectorId()
        ).orElseThrow(() ->
                new RuntimeException("Connector not found")
        );

        // 3. Validate booking time
        if (!request.getStartTime().isBefore(request.getEndTime())) {

            throw new RuntimeException(
                    "Start time must be before end time"
            );
        }

        // 4. Get charger type
        String chargerType =
                connector.getCharger().getChargerType();

        // 5. Find pricing for charger type
        ChargerPricing pricing =
                chargerPricingRepository.findByChargerType(chargerType)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Pricing not configured for charger type: "
                                                + chargerType
                                )
                        );

        /*
         * We only need the pricing record at booking time.
         *
         * Actual amount will be calculated later:
         *
         * amount = energyConsumed × pricePerKwh
         *
         * because actual energy consumption is not known
         * when the booking is created.
         */
        pricing.getPricePerKwh();

        // 6. Check existing bookings for this connector
        List<Booking> existingBookings =
                bookingRepository.findAll();

        LocalDateTime newStart =
                request.getStartTime();

        LocalDateTime newEnd =
                request.getEndTime();

        // 7. Check overlap + 15 minute buffer
        for (Booking existingBooking : existingBookings) {

            // Different connector → ignore
            if (!existingBooking.getConnector()
                    .getId()
                    .equals(connector.getId())) {

                continue;
            }

            // Cancelled booking does not block the connector
            if (existingBooking.getStatus()
                    == BookingStatus.CANCELLED) {

                continue;
            }

            LocalDateTime blockedStart =
                    existingBooking.getStartTime()
                            .minusMinutes(BOOKING_BUFFER_MINUTES);

            LocalDateTime blockedEnd =
                    existingBooking.getEndTime()
                            .plusMinutes(BOOKING_BUFFER_MINUTES);

            boolean conflict =
                    newStart.isBefore(blockedEnd)
                            && newEnd.isAfter(blockedStart);

            if (conflict) {

                throw new RuntimeException(
                        "Connector is already booked for the selected time. "
                                + "A 15-minute transition buffer is required."
                );
            }
        }

        // 8. Create booking
        Booking booking = new Booking();

        booking.setUser(user);
        booking.setConnector(connector);
        booking.setStartTime(newStart);
        booking.setEndTime(newEnd);

        /*
         * Amount is not calculated here because actual kWh
         * consumption is not known yet.
         *
         * It will be calculated after charging:
         *
         * amount = energyConsumed × pricePerKwh
         */

        booking.setStatus(BookingStatus.CONFIRMED);

        // 9. Save booking
        return bookingRepository.save(booking);
    }
}