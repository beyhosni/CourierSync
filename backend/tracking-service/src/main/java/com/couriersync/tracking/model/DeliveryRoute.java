package com.couriersync.tracking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "delivery_routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRoute {

    @Id
    private String id;

    @Field("delivery_id")
    private UUID deliveryId;

    @Field("driver_id")
    private UUID driverId;

    @Field("pickup_location")
    private Location pickupLocation;

    @Field("dropoff_location")
    private Location dropoffLocation;

    @Field("route_points")
    private List<LocationPoint> routePoints;

    @Field("estimated_distance")
    private BigDecimal estimatedDistance; // in km

    @Field("estimated_duration")
    private Integer estimatedDuration; // in minutes

    @Field("actual_distance")
    private BigDecimal actualDistance; // in km

    @Field("actual_duration")
    private Integer actualDuration; // in minutes

    @Field("status")
    private RouteStatus status;

    @Field("started_at")
    private LocalDateTime startedAt;

    @Field("completed_at")
    private LocalDateTime completedAt;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    public enum RouteStatus {
        PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        @Field("name")
        private String name;

        @Field("address")
        private String address;

        @Field("latitude")
        private BigDecimal latitude;

        @Field("longitude")
        private BigDecimal longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationPoint {
        @Field("latitude")
        private BigDecimal latitude;

        @Field("longitude")
        private BigDecimal longitude;

        @Field("timestamp")
        private LocalDateTime timestamp;

        @Field("speed")
        private BigDecimal speed;

        @Field("heading")
        private BigDecimal heading;
    }
}
