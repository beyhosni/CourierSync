package com.couriersync.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeliveryEvent extends BaseEvent {
    private UUID deliveryId;
    private String orderNumber;
    private UUID customerId;
    private UUID driverId;
    private DeliveryStatus status;
    private LocalDateTime statusChangedAt;
    private String statusChangeReason;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;
    private BigDecimal dropoffLatitude;
    private BigDecimal dropoffLongitude;
    private LocalDateTime actualPickupTime;
    private LocalDateTime actualDeliveryTime;
    private String notes;

    public DeliveryEvent(String sourceService) {
        super("delivery.event", sourceService);
    }

    public enum DeliveryStatus {
        CREATED, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED
    }
}
