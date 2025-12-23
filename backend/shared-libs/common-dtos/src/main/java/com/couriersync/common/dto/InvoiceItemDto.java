package com.couriersync.common.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDto {
    private UUID id;

    @NotNull(message = "Item type is required")
    private ItemType itemType;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Unit price must be greater than or equal to 0")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount percent must be greater than or equal to 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Discount percent must be less than or equal to 100")
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @NotNull(message = "Line total is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Line total must be greater than or equal to 0")
    private BigDecimal lineTotal;

    // Reference to related delivery (if applicable)
    private UUID deliveryId;

    public enum ItemType {
        DELIVERY, SURCHARGE, DISCOUNT, OTHER
    }
}
