package com.couriersync.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InvoiceEvent extends BaseEvent {
    private UUID invoiceId;
    private String invoiceNumber;
    private UUID customerId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentMethod;
    private LocalDate paymentDate;
    private String paymentReference;
    private LocalDateTime statusChangedAt;
    private String notes;

    public InvoiceEvent(String sourceService) {
        super("invoice.event", sourceService);
    }

    public enum InvoiceStatus {
        DRAFT, SENT, PAID, OVERDUE, CANCELLED
    }
}
