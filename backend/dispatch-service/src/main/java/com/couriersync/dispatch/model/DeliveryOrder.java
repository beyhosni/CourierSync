package com.couriersync.dispatch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DeliveryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    // Pickup information
    @Column(name = "pickup_name", nullable = false)
    private String pickupName;

    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "pickup_city", nullable = false)
    private String pickupCity;

    @Column(name = "pickup_postal_code", nullable = false)
    private String pickupPostalCode;

    @Column(name = "pickup_latitude")
    private BigDecimal pickupLatitude;

    @Column(name = "pickup_longitude")
    private BigDecimal pickupLongitude;

    @Column(name = "pickup_contact_name")
    private String pickupContactName;

    @Column(name = "pickup_contact_phone")
    private String pickupContactPhone;

    @Column(name = "pickup_notes")
    private String pickupNotes;

    // Dropoff information
    @Column(name = "dropoff_name", nullable = false)
    private String dropoffName;

    @Column(name = "dropoff_address", nullable = false)
    private String dropoffAddress;

    @Column(name = "dropoff_city", nullable = false)
    private String dropoffCity;

    @Column(name = "dropoff_postal_code", nullable = false)
    private String dropoffPostalCode;

    @Column(name = "dropoff_latitude")
    private BigDecimal dropoffLatitude;

    @Column(name = "dropoff_longitude")
    private BigDecimal dropoffLongitude;

    @Column(name = "dropoff_contact_name")
    private String dropoffContactName;

    @Column(name = "dropoff_contact_phone")
    private String dropoffContactPhone;

    @Column(name = "dropoff_notes")
    private String dropoffNotes;

    // Delivery details
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    private Status status = Status.CREATED;

    @Column(name = "package_description")
    private String packageDescription;

    @Column(name = "package_weight")
    private BigDecimal packageWeight;

    @Column(name = "is_medical_specimen")
    private Boolean isMedicalSpecimen = true;

    @Column(name = "temperature_controlled")
    private Boolean temperatureControlled = false;

    // Timing
    @Column(name = "requested_pickup_time")
    private LocalDateTime requestedPickupTime;

    @Column(name = "actual_pickup_time")
    private LocalDateTime actualPickupTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    // Assignment
    @Column(name = "assigned_driver_id")
    private UUID assignedDriverId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    // Metadata
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }

    public enum Status {
        CREATED, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED
    }
}
