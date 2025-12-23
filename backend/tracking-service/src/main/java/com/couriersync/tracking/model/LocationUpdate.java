package com.couriersync.tracking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "location_updates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdate {

    @Id
    private String id;

    @Field("driver_id")
    private UUID driverId;

    @Field("delivery_id")
    private UUID deliveryId;

    @Field("latitude")
    private BigDecimal latitude;

    @Field("longitude")
    private BigDecimal longitude;

    @Field("accuracy")
    private BigDecimal accuracy;

    @Field("speed")
    private BigDecimal speed;

    @Field("heading")
    private BigDecimal heading;

    @Field("timestamp")
    private LocalDateTime timestamp;

    @Field("battery_level")
    private Integer batteryLevel;

    @Field("device_id")
    private String deviceId;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
}
