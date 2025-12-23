package com.couriersync.common.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private UUID id;

    @NotBlank(message = "Invoice number is required")
    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    private String invoiceNumber;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String customerName;

    @Size(max = 500, message = "Customer address must not exceed 500 characters")
    private String customerAddress;

    @Size(max = 100, message = "Customer city must not exceed 100 characters")
    private String customerCity;

    @Size(max = 20, message = "Customer postal code must not exceed 20 characters")
    private String customerPostalCode;

    @Size(max = 100, message = "Customer country must not exceed 100 characters")
    private String customerCountry;

    // Invoice details
    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Status is required")
    private InvoiceStatus status;

    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Subtotal must be greater than or equal to 0")
    private BigDecimal subtotal;

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tax rate must be greater than or equal to 0")
    private BigDecimal taxRate;

    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tax amount must be greater than or equal to 0")
    private BigDecimal taxAmount;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total amount must be greater than or equal to 0")
    private BigDecimal totalAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency = "USD";

    // Payment details
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    private LocalDate paymentDate;

    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    private String paymentReference;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // Invoice items
    private List<InvoiceItemDto> items;

    public enum InvoiceStatus {
        DRAFT, SENT, PAID, OVERDUE, CANCELLED
    }
}
