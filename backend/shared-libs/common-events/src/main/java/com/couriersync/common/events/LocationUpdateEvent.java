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
public class LocationUpdateEvent extends BaseEvent {
    private UUID driverId;
    private UUID deliveryId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal accuracy;
    private BigDecimal speed;
    private BigDecimal heading;
    private LocalDateTime timestamp;
    private Integer batteryLevel;
    private String deviceId;

    public LocationUpdateEvent(String sourceService) {
        super("location.update", sourceService);
    }
}
