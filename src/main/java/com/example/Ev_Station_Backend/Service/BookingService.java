package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.Enum.BookingStatus;
import com.example.Ev_Station_Backend.Enum.PaymentStatus;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private static final long BOOKING_BUFFER_MINUTES = 15;

    /*
     * Business rules
     */
    private static final BigDecimal ESTIMATED_KWH_PER_HOUR =
            BigDecimal.valueOf(5);

    private static final BigDecimal ADVANCE_PERCENTAGE =
            BigDecimal.valueOf(0.20);

    private static final long PAYMENT_EXPIRY_MINUTES = 15;

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

        BigDecimal pricePerKwh =
                pricing.getPricePerKwh();

        // 6. Check existing bookings for this connector
        List<Booking> existingBookings =
                bookingRepository.findAll();

        LocalDateTime newStart =
                request.getStartTime();

        LocalDateTime newEnd =
                request.getEndTime();

        LocalDateTime now = LocalDateTime.now();

        // 7. Check overlap + 15 minute buffer
        for (Booking existingBooking : existingBookings) {

            // Different connector → ignore
            if (!existingBooking.getConnector()
                    .getId()
                    .equals(connector.getId())) {

                continue;
            }

            /*
             * Cancelled booking does not block connector
             */
            if (existingBooking.getStatus()
                    == BookingStatus.CANCELLED) {

                continue;
            }

            /*
             * Expired payment booking does not block connector
             *
             * If payment time has expired, ignore it.
             */
            if (existingBooking.getStatus()
                    == BookingStatus.EXPIRED) {

                continue;
            }

            /*
             * PENDING booking:
             *
             * It blocks the connector only while
             * payment expiry time has not passed.
             */
            if (existingBooking.getStatus()
                    == BookingStatus.PENDING) {

                if (existingBooking.getPaymentExpiresAt() != null &&
                        existingBooking.getPaymentExpiresAt()
                                .isBefore(now)) {

                    continue;
                }
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

        /*
         * 8. Calculate booking duration
         */
        long durationMinutes =
                Duration.between(newStart, newEnd).toMinutes();

        BigDecimal durationHours =
                BigDecimal.valueOf(durationMinutes)
                        .divide(
                                BigDecimal.valueOf(60),
                                2,
                                RoundingMode.HALF_UP
                        );

        /*
         * 9. Estimate energy consumption
         *
         * Business assumption:
         * 5 kWh per booking hour
         */
        BigDecimal estimatedEnergy =
                durationHours.multiply(
                        ESTIMATED_KWH_PER_HOUR
                );

        /*
         * 10. Calculate estimated amount
         *
         * Estimated Amount =
         * Estimated Energy × Price Per kWh
         */
        BigDecimal estimatedAmount =
                estimatedEnergy.multiply(pricePerKwh)
                        .setScale(2, RoundingMode.HALF_UP);

        /*
         * 11. Calculate 20% advance
         */
        BigDecimal advanceAmount =
                estimatedAmount.multiply(
                                ADVANCE_PERCENTAGE
                        )
                        .setScale(2, RoundingMode.HALF_UP);

        /*
         * 12. Create booking
         */
        Booking booking = new Booking();

        booking.setUser(user);
        booking.setConnector(connector);
        booking.setStartTime(newStart);
        booking.setEndTime(newEnd);

        booking.setEstimatedAmount(estimatedAmount);
        booking.setAdvanceAmount(advanceAmount);

        /*
         * Final amount is not known at booking time.
         */
        booking.setFinalAmount(null);

        /*
         * Payment is pending until payment is completed.
         */
        booking.setPaymentStatus(PaymentStatus.PENDING);

        /*
         * Booking is pending until advance payment succeeds.
         */
        booking.setStatus(BookingStatus.PENDING);

        /*
         * Customer gets 15 minutes to complete payment.
         */
        booking.setPaymentExpiresAt(
                now.plusMinutes(PAYMENT_EXPIRY_MINUTES)
        );

        /*
         * Cancellation values are initially zero/null.
         */
        booking.setCancellationCharge(BigDecimal.ZERO);
        booking.setRefundAmount(BigDecimal.ZERO);

        /*
         * 13. Save booking
         */
        return bookingRepository.save(booking);
        }

        public Booking confirmPayment(Long bookingId) {

        User currentUser = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException(
                        "You are not allowed to confirm payment for this booking"
                );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new RuntimeException("Booking is cancelled");
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
                throw new RuntimeException("Booking payment has expired");
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
                throw new RuntimeException("Payment is already confirmed");
        }

        if (booking.getPaymentExpiresAt() != null &&
                booking.getPaymentExpiresAt().isBefore(LocalDateTime.now())) {

                booking.setStatus(BookingStatus.EXPIRED);
                booking.setPaymentStatus(PaymentStatus.FAILED);

                return bookingRepository.save(booking);
        }

        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentExpiresAt(null);
        return bookingRepository.save(booking);
        }
}

