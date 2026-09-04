package com.example.Ev_Station_Backend.entity;

import com.example.Ev_Station_Backend.Enum.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "connector_id", nullable = false)
    private Connector connector;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = true)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "cancellation_charge")
    private BigDecimal cancellationCharge;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
    if (status == null) {
        status = BookingStatus.CONFIRMED;
    }

    if (createdAt == null) {
        createdAt = LocalDateTime.now();
    }
}
}