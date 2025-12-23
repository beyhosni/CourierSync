package com.couriersync.billing.consumer;

import com.couriersync.common.events.DeliveryEvent;
import com.couriersync.billing.model.Invoice;
import com.couriersync.billing.model.InvoiceItem;
import com.couriersync.billing.service.InvoiceService;
import com.couriersync.billing.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final InvoiceService invoiceService;
    private final PricingService pricingService;

    @KafkaListener(topics = "${app.kafka.topics.delivery-events}", groupId = "${spring.application.name}")
    public void handleDeliveryEvent(@Payload DeliveryEvent event) {
        log.info("Received delivery event: {} for delivery: {}", event.getEventType(), event.getDeliveryId());

        try {
            switch (event.getEventType()) {
                case "delivery.completed":
                    // Create an invoice when a delivery is completed
                    if (event.getDeliveryId() != null && event.getCustomerId() != null) {
                        createInvoiceForCompletedDelivery(event);
                    }
                    break;

                default:
                    log.debug("Ignoring delivery event: {} for billing service", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing delivery event: {}", event, e);
        }
    }

    private void createInvoiceForCompletedDelivery(DeliveryEvent event) {
        // In a real system, this would fetch the full delivery details from the dispatch service
        // For now, we'll create a simplified invoice based on the event data

        // Calculate price using pricing service
        PricingService.PricingCalculation calculation = pricingService.calculateDeliveryPrice(
                event.getCustomerId(),
                PricingRule.CustomerType.MEDICAL_FACILITY, // Assuming medical facility for medical deliveries
                PricingRule.PriorityLevel.valueOf(event.getStatus().name()),
                10.0, // Example distance in km
                1.0,  // Example weight in kg
                java.time.LocalDateTime.now()
        );

        // Create invoice
        Invoice invoice = Invoice.builder()
                .customerId(event.getCustomerId())
                .customerName("Customer Name") // In a real system, fetch from customer service
                .customerAddress("Customer Address") // In a real system, fetch from customer service
                .customerCity("Customer City") // In a real system, fetch from customer service
                .customerPostalCode("12345") // In a real system, fetch from customer service
                .customerCountry("Country") // In a real system, fetch from customer service
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30)) // Due in 30 days
                .status(Invoice.InvoiceStatus.DRAFT)
                .taxRate(new BigDecimal("0.10")) // 10% tax
                .notes("Invoice for delivery: " + event.getOrderNumber())
                .build();

        // Create invoice items
        List<InvoiceItem> items = new ArrayList<>();

        // Base delivery fee
        items.add(InvoiceItem.builder()
                .itemType(InvoiceItem.ItemType.DELIVERY)
                .description("Medical Delivery - " + event.getOrderNumber())
                .quantity(1)
                .unitPrice(calculation.getBaseRate())
                .lineTotal(calculation.getBaseRate())
                .deliveryId(event.getDeliveryId())
                .build());

        // Distance charge
        if (calculation.getDistanceCharge().compareTo(BigDecimal.ZERO) > 0) {
            items.add(InvoiceItem.builder()
                    .itemType(InvoiceItem.ItemType.DELIVERY)
                    .description("Distance charge")
                    .quantity(1)
                    .unitPrice(calculation.getDistanceCharge())
                    .lineTotal(calculation.getDistanceCharge())
                    .deliveryId(event.getDeliveryId())
                    .build());
        }

        // Urgent surcharge
        if (calculation.getUrgentSurcharge().compareTo(BigDecimal.ZERO) > 0) {
            items.add(InvoiceItem.builder()
                    .itemType(InvoiceItem.ItemType.SURCHARGE)
                    .description("Urgent delivery surcharge")
                    .quantity(1)
                    .unitPrice(calculation.getUrgentSurcharge())
                    .lineTotal(calculation.getUrgentSurcharge())
                    .deliveryId(event.getDeliveryId())
                    .build());
        }

        // After-hours surcharge
        if (calculation.getAfterHoursSurcharge().compareTo(BigDecimal.ZERO) > 0) {
            items.add(InvoiceItem.builder()
                    .itemType(InvoiceItem.ItemType.SURCHARGE)
                    .description("After-hours delivery surcharge")
                    .quantity(1)
                    .unitPrice(calculation.getAfterHoursSurcharge())
                    .lineTotal(calculation.getAfterHoursSurcharge())
                    .deliveryId(event.getDeliveryId())
                    .build());
        }

        // Weekend surcharge
        if (calculation.getWeekendSurcharge().compareTo(BigDecimal.ZERO) > 0) {
            items.add(InvoiceItem.builder()
                    .itemType(InvoiceItem.ItemType.SURCHARGE)
                    .description("Weekend delivery surcharge")
                    .quantity(1)
                    .unitPrice(calculation.getWeekendSurcharge())
                    .lineTotal(calculation.getWeekendSurcharge())
                    .deliveryId(event.getDeliveryId())
                    .build());
        }

        // Weight surcharge
        if (calculation.getWeightSurcharge().compareTo(BigDecimal.ZERO) > 0) {
            items.add(InvoiceItem.builder()
                    .itemType(InvoiceItem.ItemType.SURCHARGE)
                    .description("Weight surcharge")
                    .quantity(1)
                    .unitPrice(calculation.getWeightSurcharge())
                    .lineTotal(calculation.getWeightSurcharge())
                    .deliveryId(event.getDeliveryId())
                    .build());
        }

        // Extra distance surcharge
        if (calculation.getDistanceSurcharge().compareTo(BigDecimal.ZERO) > 0) {
            items.add(InvoiceItem.builder()
                    .itemType(InvoiceItem.ItemType.SURCHARGE)
                    .description("Extra distance surcharge")
                    .quantity(1)
                    .unitPrice(calculation.getDistanceSurcharge())
                    .lineTotal(calculation.getDistanceSurcharge())
                    .deliveryId(event.getDeliveryId())
                    .build());
        }

        // Create the invoice with items
        invoiceService.createInvoice(invoice, items);

        log.info("Created invoice for completed delivery: {}", event.getDeliveryId());
    }
}
