package com.couriersync.common.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateDto {
    private UUID id;

    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    private UUID deliveryId;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    @DecimalMin(value = "0.0", inclusive = true, message = "Accuracy must be greater than or equal to 0")
    private BigDecimal accuracy;

    @DecimalMin(value = "0.0", inclusive = true, message = "Speed must be greater than or equal to 0")
    private BigDecimal speed;

    @DecimalMin(value = "0.0", inclusive = true, message = "Heading must be greater than or equal to 0")
    @DecimalMax(value = "360.0", inclusive = true, message = "Heading must be less than or equal to 360")
    private BigDecimal heading;

    private LocalDateTime timestamp;

    @Min(value = 0, message = "Battery level must be between 0 and 100")
    @Max(value = 100, message = "Battery level must be between 0 and 100")
    private Integer batteryLevel;

    @Size(max = 100, message = "Device ID must not exceed 100 characters")
    private String deviceId;
}
