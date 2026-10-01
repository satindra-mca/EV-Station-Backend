package com.example.Ev_Station_Backend.Service;

import com.example.Ev_Station_Backend.Enum.BookingStatus;
import com.example.Ev_Station_Backend.Enum.PaymentStatus;
import com.example.Ev_Station_Backend.Repository.BookingRepository;
import com.example.Ev_Station_Backend.Repository.ChargerPricingRepository;
import com.example.Ev_Station_Backend.Repository.ConnectorRepository;
import com.example.Ev_Station_Backend.Repository.UserRepository;
import com.example.Ev_Station_Backend.dto.BookingRequest;
import com.example.Ev_Station_Backend.dto.PaymentRequest;
import com.example.Ev_Station_Backend.entity.Booking;
import com.example.Ev_Station_Backend.entity.ChargerPricing;
import com.example.Ev_Station_Backend.entity.Connector;
import com.example.Ev_Station_Backend.entity.User;
import com.example.Ev_Station_Backend.exception.BookingException;


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
        private static final long FULL_REFUND_HOURS = 8;
        private static final long PARTIAL_REFUND_HOURS = 5;

        private static final BigDecimal PARTIAL_CANCELLATION_CHARGE =
                BigDecimal.valueOf(0.20);

        private static final BigDecimal LATE_CANCELLATION_CHARGE =
                BigDecimal.valueOf(0.50);

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


        public Booking cancelBooking(
                Long bookingId,
                String reason) {

        // 1. Get currently logged-in user
        User currentUser = getCurrentUser();

        // 2. Find booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        // 3. Check booking ownership
        if (!booking.getUser().getId().equals(currentUser.getId())) {

                throw new RuntimeException(
                        "You are not allowed to cancel this booking");
        }

        // 4. Already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {

                throw new RuntimeException(
                        "Booking is already cancelled");
        }

        // 5. Expired booking cannot be cancelled
        if (booking.getStatus() == BookingStatus.EXPIRED) {

                throw new RuntimeException(
                        "Expired booking cannot be cancelled");
        }

        // 6. Completed booking cannot be cancelled
        if (booking.getStatus() == BookingStatus.COMPLETED) {

                throw new RuntimeException(
                        "Completed booking cannot be cancelled");
        }

        LocalDateTime now = LocalDateTime.now();

        // 7. Cancellation after booking start is not allowed
        if (!now.isBefore(booking.getStartTime())) {

                throw new RuntimeException(
                        "Booking cannot be cancelled after the start time");
        }

        /*
        * 8. PENDING + PENDING
        *
        * Advance payment has not been completed.
        * Therefore there is no money to refund.
        */
        if (booking.getStatus() == BookingStatus.PENDING &&
                booking.getPaymentStatus() == PaymentStatus.PENDING) {

                booking.setStatus(BookingStatus.CANCELLED);
                booking.setPaymentStatus(PaymentStatus.CANCELLED);

                booking.setCancellationCharge(BigDecimal.ZERO);
                booking.setRefundAmount(BigDecimal.ZERO);
                booking.setCancelledAt(now);

                return bookingRepository.save(booking);
        }

        /*
        * 9. CONFIRMED + PAID
        *
        * Calculate cancellation charge based on
        * remaining time before booking start.
        */
        if (booking.getStatus() == BookingStatus.CONFIRMED &&
                booking.getPaymentStatus() == PaymentStatus.PAID) {

                Duration remainingTime =
                        Duration.between(now, booking.getStartTime());

                BigDecimal chargePercentage;



                /*
                * More than 8 hours
                * → 0% cancellation charge
                */
                if (remainingTime.compareTo(
                        Duration.ofHours(FULL_REFUND_HOURS)) > 0) {

                chargePercentage = BigDecimal.ZERO;
                }
                else if (remainingTime.compareTo(
                        Duration.ofHours(PARTIAL_REFUND_HOURS)) >= 0) {

                chargePercentage =
                        PARTIAL_CANCELLATION_CHARGE;
                }
                else {

                chargePercentage =
                        LATE_CANCELLATION_CHARGE;
                }
                BigDecimal advanceAmount =
                        booking.getAdvanceAmount();

                BigDecimal cancellationCharge =
                        advanceAmount
                                .multiply(chargePercentage)
                                .setScale(2, RoundingMode.HALF_UP);

                BigDecimal refundAmount =
                        advanceAmount
                                .subtract(cancellationCharge)
                                .setScale(2, RoundingMode.HALF_UP);

                booking.setCancellationCharge(
                        cancellationCharge);

                booking.setRefundAmount(
                        refundAmount);

                booking.setStatus(
                        BookingStatus.CANCELLED);

                booking.setCancelledAt(now);

                /*
                * Full refund
                */
                if (refundAmount.compareTo(BigDecimal.ZERO) > 0 &&
                        cancellationCharge.compareTo(BigDecimal.ZERO) == 0) {

                booking.setPaymentStatus(
                        PaymentStatus.REFUNDED);
                }

                /*
                * Partial refund
                */
                else if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {

                booking.setPaymentStatus(
                        PaymentStatus.PARTIALLY_REFUNDED);
                }

                /*
                * No refund
                */
                else {

                booking.setPaymentStatus(
                        PaymentStatus.CANCELLED);
                }

                return bookingRepository.save(booking);
        }

        throw new RuntimeException(
                "Booking cannot be cancelled in its current state");
        }

        public Booking confirmPayment(Long bookingId, PaymentRequest request) {

                        User currentUser = getCurrentUser();

                Booking booking = bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new RuntimeException("Booking not found"));

                if (!booking.getUser().getId().equals(currentUser.getId())) {
                        throw new RuntimeException(
                                "You are not allowed to confirm payment for this booking");
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

                LocalDateTime now = LocalDateTime.now();

                if (booking.getPaymentExpiresAt() != null &&
                        booking.getPaymentExpiresAt().isBefore(now)) {

                        booking.setStatus(BookingStatus.EXPIRED);
                        booking.setPaymentStatus(PaymentStatus.FAILED);

                        return bookingRepository.save(booking);
                }

                BigDecimal requiredAdvance = booking.getAdvanceAmount();

                if (request.getAmount() == null) {
                        throw new RuntimeException("Payment amount is required");
                }

                if (request.getAmount().compareTo(requiredAdvance) != 0) {
                        throw new BookingException("Incorrect advance payment amount. Required amount: "+ requiredAdvance);
                }

                booking.setPaymentStatus(PaymentStatus.PAID);
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setPaymentExpiresAt(null);

                return bookingRepository.save(booking);
        }
}

