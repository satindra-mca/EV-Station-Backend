package com.example.Ev_Station_Backend.Service.booking;

import com.example.Ev_Station_Backend.Enum.BookingStatus;
import com.example.Ev_Station_Backend.Enum.PaymentStatus;
import com.example.Ev_Station_Backend.Repository.BookingRepository;
import com.example.Ev_Station_Backend.Repository.ChargerPricingRepository;
import com.example.Ev_Station_Backend.Repository.ConnectorRepository;
import com.example.Ev_Station_Backend.Repository.UserRepository;
import com.example.Ev_Station_Backend.dto.booking.BookingRequest;
import com.example.Ev_Station_Backend.dto.booking.PaymentRequest;
import com.example.Ev_Station_Backend.entity.Booking;
import com.example.Ev_Station_Backend.entity.ChargerPricing;
import com.example.Ev_Station_Backend.entity.Connector;
import com.example.Ev_Station_Backend.entity.User;
import com.example.Ev_Station_Backend.exception.BookingException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import com.example.Ev_Station_Backend.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.example.Ev_Station_Backend.dto.booking.BookingPreviewResponse;

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

    private static final long MIN_BOOKING_DURATION_MINUTES = 30;



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
        /**
         * Validates the requested booking time range.
         */
        private void validateBookingTime(
                BookingRequest request,
                LocalDateTime now) {

        if (!request.getStartTime().isAfter(now)) {

                throw new BookingException(
                        "Booking start time must be in the future"
                );
        }

        if (!request.getStartTime()
                .isBefore(request.getEndTime())) {

                throw new BookingException(
                        "Start time must be before end time"
                );
        }

        Duration duration = Duration.between(
                request.getStartTime(),
                request.getEndTime()
        );

        if (duration.compareTo(
                Duration.ofMinutes(
                        MIN_BOOKING_DURATION_MINUTES
                )) < 0) {

                throw new BookingException(
                        "Minimum booking duration is 30 minutes"
                );
        }
        }

        /**
         * Validates whether the connector supports reservations.
         *
         * Current occupancy does not determine future slot
         * availability. Booking overlap is checked separately.
         */
        private void validateConnectorStatus(
                Connector connector) {

        String status = connector.getStatus();

        boolean supportedStatus =
                "AVAILABLE".equalsIgnoreCase(status)
                || "OCCUPIED".equalsIgnoreCase(status);

        if (!supportedStatus) {

                throw new BookingException(
                        "Connector is not available for booking"
                );
        }
        }

        /**
         * Checks booking conflicts, including the transition buffer.
         */
        private boolean hasBookingConflict(
                Long connectorId,
                LocalDateTime newStart,
                LocalDateTime newEnd,
                LocalDateTime now) {

        List<Booking> existingBookings =
                bookingRepository.findAllByConnector_Id(
                        connectorId
                );

        for (Booking existingBooking : existingBookings) {

                BookingStatus status =
                        existingBooking.getStatus();

                if (status != BookingStatus.PENDING
                        && status != BookingStatus.CONFIRMED) {

                continue;
                }

                if (status == BookingStatus.PENDING) {

                LocalDateTime expiresAt =
                        existingBooking.getPaymentExpiresAt();

                if (expiresAt == null
                        || !expiresAt.isAfter(now)) {

                        continue;
                }
                }

                LocalDateTime blockedStart =
                        existingBooking.getStartTime()
                                .minusMinutes(
                                        BOOKING_BUFFER_MINUTES
                                );

                LocalDateTime blockedEnd =
                        existingBooking.getEndTime()
                                .plusMinutes(
                                        BOOKING_BUFFER_MINUTES
                                );

                if (newStart.isBefore(blockedEnd)
                        && newEnd.isAfter(blockedStart)) {

                return true;
                }
        }

        return false;
        }

        /**
         * Retrieves the configured charger price.
         */
        private BigDecimal getBookingPricePerKwh(
                Connector connector) {

        String chargerType =
                connector.getCharger().getChargerType();

        ChargerPricing pricing =
                chargerPricingRepository
                        .findByChargerType(chargerType)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Pricing not configured for charger type: "
                                                + chargerType
                                )
                        );

        return pricing.getPricePerKwh();
        }

        /**
         * Calculates authoritative booking estimates.
         */
        private BookingEstimate calculateBookingEstimate(
                Connector connector,
                LocalDateTime startTime,
                LocalDateTime endTime) {

        long durationMinutes =
                Duration.between(
                        startTime,
                        endTime
                ).toMinutes();

        BigDecimal durationHours =
                BigDecimal.valueOf(durationMinutes)
                        .divide(
                                BigDecimal.valueOf(60),
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal pricePerKwh =
                getBookingPricePerKwh(connector);

        BigDecimal estimatedEnergy =
                durationHours.multiply(
                        ESTIMATED_KWH_PER_HOUR
                );

        BigDecimal estimatedAmount =
                estimatedEnergy.multiply(pricePerKwh)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal advanceAmount =
                estimatedAmount.multiply(
                        ADVANCE_PERCENTAGE
                ).setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        return new BookingEstimate(
                durationMinutes,
                pricePerKwh,
                estimatedEnergy,
                estimatedAmount,
                advanceAmount
        );
        }

        /**
         * Internal calculation result shared by booking operations.
         */
        private record BookingEstimate(
                long durationMinutes,
                BigDecimal pricePerKwh,
                BigDecimal estimatedEnergy,
                BigDecimal estimatedAmount,
                BigDecimal advanceAmount
        ) {
        }

        /**
         * Checks slot availability and calculates booking
         * estimates without creating or reserving a booking.
         */
        @Transactional(readOnly = true)
        public BookingPreviewResponse previewBooking(
                BookingRequest request) {

        // 1. Verify authenticated user.
        getCurrentUser();

        LocalDateTime now = LocalDateTime.now();

        // 2. Validate requested time and duration.
        validateBookingTime(request, now);

        // 3. Retrieve the selected connector.
        Connector connector =
                connectorRepository.findById(
                        request.getConnectorId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Connector not found"
                        )
                );

        // 4. Validate supported connector status.
        validateConnectorStatus(connector);

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();

        // 5. Prepare the preview response.
        BookingPreviewResponse response =
                new BookingPreviewResponse();

        response.setConnectorId(connector.getId());
        response.setStartTime(startTime);
        response.setEndTime(endTime);

        response.setDurationMinutes(
                Duration.between(
                        startTime,
                        endTime
                ).toMinutes()
        );

        // 6. Check existing booking conflicts.
        if (hasBookingConflict(
                connector.getId(),
                startTime,
                endTime,
                now)) {

                response.setAvailable(false);

                response.setMessage(
                        "Selected slot is unavailable. "
                                + "Please select another time slot. "
                                + "A 15-minute transition buffer is required."
                );

                return response;
        }

        // 7. Calculate server-authoritative estimates.
        BookingEstimate estimate =
                calculateBookingEstimate(
                        connector,
                        startTime,
                        endTime
                );

        response.setAvailable(true);

        response.setDurationMinutes(
                estimate.durationMinutes()
        );

        response.setPricePerKwh(
                estimate.pricePerKwh()
        );

        response.setEstimatedEnergy(
                estimate.estimatedEnergy()
        );

        response.setEstimatedAmount(
                estimate.estimatedAmount()
        );

        response.setAdvanceAmount(
                estimate.advanceAmount()
        );

        response.setMessage(
                "Selected slot is available"
        );

        return response;
        }

        /**
         * Creates a pending booking after validating the
         * requested slot under a pessimistic connector lock.
         */
        @Transactional(isolation = Isolation.READ_COMMITTED)
        public Booking createBooking(BookingRequest request) {

        // 1. Retrieve the authenticated user.
        User user = getCurrentUser();

        // 2. Lock the selected connector.
        Connector connector =
                connectorRepository.findByIdForUpdate(
                        request.getConnectorId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Connector not found"
                        )
                );

        LocalDateTime now = LocalDateTime.now();

        // 3. Validate booking time and duration.
        validateBookingTime(request, now);

        // 4. Validate supported connector status.
        validateConnectorStatus(connector);

        LocalDateTime newStart = request.getStartTime();
        LocalDateTime newEnd = request.getEndTime();

        // 5. Recheck availability while holding the lock.
        if (hasBookingConflict(
                connector.getId(),
                newStart,
                newEnd,
                now)) {

                throw new BookingException(
                        "Connector is already booked for the selected time. "
                                + "A 15-minute transition buffer is required."
                );
        }

        // 6. Calculate server-authoritative amounts.
        BookingEstimate estimate =
                calculateBookingEstimate(
                        connector,
                        newStart,
                        newEnd
                );

        // 7. Create the pending booking.
        Booking booking = new Booking();

        booking.setUser(user);
        booking.setConnector(connector);

        booking.setStartTime(newStart);
        booking.setEndTime(newEnd);

        booking.setEstimatedAmount(
                estimate.estimatedAmount()
        );

        booking.setAdvanceAmount(
                estimate.advanceAmount()
        );

        booking.setFinalAmount(null);

        booking.setPaymentStatus(
                PaymentStatus.PENDING
        );

        booking.setStatus(
                BookingStatus.PENDING
        );

        booking.setPaymentExpiresAt(
                now.plusMinutes(PAYMENT_EXPIRY_MINUTES)
        );

        booking.setCancellationCharge(
                BigDecimal.ZERO
        );

        booking.setRefundAmount(
                BigDecimal.ZERO
        );

        // 8. Persist the booking.
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
                                new ResourceNotFoundException("Booking not found"));

                if (!booking.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(
                        "You are not allowed to confirm payment for this booking");
                }

                if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new BookingException("Booking is cancelled");
                }

                if (booking.getStatus() == BookingStatus.EXPIRED) {
                throw new BookingException("Booking payment has expired");
                }

                if (booking.getPaymentStatus() == PaymentStatus.PAID) {
                throw new BookingException("Payment is already confirmed");
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

