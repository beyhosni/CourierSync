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
public class DeliveryRequestDto {
    private UUID id;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    // Pickup information
    @NotBlank(message = "Pickup name is required")
    @Size(max = 255, message = "Pickup name must not exceed 255 characters")
    private String pickupName;

    @NotBlank(message = "Pickup address is required")
    @Size(max = 500, message = "Pickup address must not exceed 500 characters")
    private String pickupAddress;

    @NotBlank(message = "Pickup city is required")
    @Size(max = 100, message = "Pickup city must not exceed 100 characters")
    private String pickupCity;

    @NotBlank(message = "Pickup postal code is required")
    @Size(max = 20, message = "Pickup postal code must not exceed 20 characters")
    private String pickupPostalCode;

    @DecimalMin(value = "-90.0", message = "Pickup latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Pickup latitude must be between -90 and 90")
    private BigDecimal pickupLatitude;

    @DecimalMin(value = "-180.0", message = "Pickup longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Pickup longitude must be between -180 and 180")
    private BigDecimal pickupLongitude;

    @Size(max = 100, message = "Pickup contact name must not exceed 100 characters")
    private String pickupContactName;

    @Size(max = 20, message = "Pickup contact phone must not exceed 20 characters")
    private String pickupContactPhone;

    @Size(max = 1000, message = "Pickup notes must not exceed 1000 characters")
    private String pickupNotes;

    // Dropoff information
    @NotBlank(message = "Dropoff name is required")
    @Size(max = 255, message = "Dropoff name must not exceed 255 characters")
    private String dropoffName;

    @NotBlank(message = "Dropoff address is required")
    @Size(max = 500, message = "Dropoff address must not exceed 500 characters")
    private String dropoffAddress;

    @NotBlank(message = "Dropoff city is required")
    @Size(max = 100, message = "Dropoff city must not exceed 100 characters")
    private String dropoffCity;

    @NotBlank(message = "Dropoff postal code is required")
    @Size(max = 20, message = "Dropoff postal code must not exceed 20 characters")
    private String dropoffPostalCode;

    @DecimalMin(value = "-90.0", message = "Dropoff latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Dropoff latitude must be between -90 and 90")
    private BigDecimal dropoffLatitude;

    @DecimalMin(value = "-180.0", message = "Dropoff longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Dropoff longitude must be between -180 and 180")
    private BigDecimal dropoffLongitude;

    @Size(max = 100, message = "Dropoff contact name must not exceed 100 characters")
    private String dropoffContactName;

    @Size(max = 20, message = "Dropoff contact phone must not exceed 20 characters")
    private String dropoffContactPhone;

    @Size(max = 1000, message = "Dropoff notes must not exceed 1000 characters")
    private String dropoffNotes;

    // Delivery details
    @NotNull(message = "Priority is required")
    private Priority priority;

    @Size(max = 1000, message = "Package description must not exceed 1000 characters")
    private String packageDescription;

    @DecimalMin(value = "0.0", inclusive = false, message = "Package weight must be greater than 0")
    private BigDecimal packageWeight;

    private Boolean isMedicalSpecimen = true;

    private Boolean temperatureControlled = false;

    // Timing
    private LocalDateTime requestedPickupTime;

    private LocalDateTime estimatedDeliveryTime;

    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }
}
