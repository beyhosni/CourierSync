package com.couriersync.billing.producer;

import com.couriersync.common.events.InvoiceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.invoice-events}")
    private String invoiceEventsTopic;

    public void publishInvoiceCreatedEvent(UUID invoiceId, String invoiceNumber, UUID customerId) {
        log.info("Publishing invoice created event for invoice: {}", invoiceId);

        InvoiceEvent event = new InvoiceEvent("billing-service");
        event.setInvoiceId(invoiceId);
        event.setInvoiceNumber(invoiceNumber);
        event.setCustomerId(customerId);
        event.setStatus(InvoiceEvent.InvoiceStatus.DRAFT);
        event.setIssueDate(LocalDate.now());
        event.setStatusChangedAt(java.time.LocalDateTime.now());

        kafkaTemplate.send(invoiceEventsTopic, invoiceId.toString(), event);
    }

    public void publishInvoiceStatusUpdatedEvent(UUID invoiceId, String invoiceNumber, UUID customerId,
                                           InvoiceEvent.InvoiceStatus status, String notes) {
        log.info("Publishing invoice status updated event for invoice: {}, status: {}", invoiceId, status);

        InvoiceEvent event = new InvoiceEvent("billing-service");
        event.setInvoiceId(invoiceId);
        event.setInvoiceNumber(invoiceNumber);
        event.setCustomerId(customerId);
        event.setStatus(status);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setNotes(notes);

        kafkaTemplate.send(invoiceEventsTopic, invoiceId.toString(), event);
    }

    public void publishInvoiceSentEvent(UUID invoiceId, String invoiceNumber, UUID customerId) {
        log.info("Publishing invoice sent event for invoice: {}", invoiceId);

        InvoiceEvent event = new InvoiceEvent("billing-service");
        event.setInvoiceId(invoiceId);
        event.setInvoiceNumber(invoiceNumber);
        event.setCustomerId(customerId);
        event.setStatus(InvoiceEvent.InvoiceStatus.SENT);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setNotes("Invoice sent to customer");

        kafkaTemplate.send(invoiceEventsTopic, invoiceId.toString(), event);
    }

    public void publishInvoicePaidEvent(UUID invoiceId, String invoiceNumber, UUID customerId,
                                   String paymentMethod, LocalDate paymentDate, String paymentReference) {
        log.info("Publishing invoice paid event for invoice: {}", invoiceId);

        InvoiceEvent event = new InvoiceEvent("billing-service");
        event.setInvoiceId(invoiceId);
        event.setInvoiceNumber(invoiceNumber);
        event.setCustomerId(customerId);
        event.setStatus(InvoiceEvent.InvoiceStatus.PAID);
        event.setPaymentMethod(paymentMethod);
        event.setPaymentDate(paymentDate);
        event.setPaymentReference(paymentReference);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setNotes("Payment received");

        kafkaTemplate.send(invoiceEventsTopic, invoiceId.toString(), event);
    }

    public void publishInvoiceOverdueEvent(UUID invoiceId, String invoiceNumber, UUID customerId) {
        log.info("Publishing invoice overdue event for invoice: {}", invoiceId);

        InvoiceEvent event = new InvoiceEvent("billing-service");
        event.setInvoiceId(invoiceId);
        event.setInvoiceNumber(invoiceNumber);
        event.setCustomerId(customerId);
        event.setStatus(InvoiceEvent.InvoiceStatus.OVERDUE);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setNotes("Invoice is overdue");

        kafkaTemplate.send(invoiceEventsTopic, invoiceId.toString(), event);
    }

    public void publishInvoiceCancelledEvent(UUID invoiceId, String invoiceNumber, UUID customerId, String reason) {
        log.info("Publishing invoice cancelled event for invoice: {}, reason: {}", invoiceId, reason);

        InvoiceEvent event = new InvoiceEvent("billing-service");
        event.setInvoiceId(invoiceId);
        event.setInvoiceNumber(invoiceNumber);
        event.setCustomerId(customerId);
        event.setStatus(InvoiceEvent.InvoiceStatus.CANCELLED);
        event.setStatusChangedAt(java.time.LocalDateTime.now());
        event.setNotes(reason);

        kafkaTemplate.send(invoiceEventsTopic, invoiceId.toString(), event);
    }
}
