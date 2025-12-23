package com.couriersync.billing.service;

import com.couriersync.billing.model.Invoice;
import com.couriersync.billing.model.InvoiceItem;
import com.couriersync.billing.repository.InvoiceRepository;
import com.couriersync.billing.repository.InvoiceItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PricingService pricingService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String INVOICE_EVENTS_TOPIC = "billing.invoices";

    public Invoice createInvoice(Invoice invoice, List<InvoiceItem> items) {
        log.info("Creating new invoice for customer: {}", invoice.getCustomerId());

        // Generate unique invoice number
        String invoiceNumber = generateInvoiceNumber();
        invoice.setInvoiceNumber(invoiceNumber);

        // Set default values if not provided
        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDate.now());
        }

        if (invoice.getDueDate() == null) {
            invoice.setDueDate(invoice.getIssueDate().plusDays(30)); // Default 30 days
        }

        if (invoice.getTaxRate() == null) {
            invoice.setTaxRate(new BigDecimal("0.10")); // Default 10% tax
        }

        // Save the invoice first
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Add items and calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceItem item : items) {
            item.setInvoice(savedInvoice);

            // Calculate line total if not provided
            if (item.getLineTotal() == null) {
                BigDecimal lineTotal = item.getUnitPrice()
                        .multiply(new BigDecimal(item.getQuantity()));

                // Apply discount if any
                if (item.getDiscountPercent() != null && item.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal discountAmount = lineTotal.multiply(item.getDiscountPercent())
                            .divide(new BigDecimal("100"));
                    lineTotal = lineTotal.subtract(discountAmount);
                }

                item.setLineTotal(lineTotal);
            }

            subtotal = subtotal.add(item.getLineTotal());
        }

        // Save items
        invoiceItemRepository.saveAll(items);

        // Calculate tax and total
        BigDecimal taxAmount = subtotal.multiply(invoice.getTaxRate());
        BigDecimal totalAmount = subtotal.add(taxAmount);

        savedInvoice.setSubtotal(subtotal);
        savedInvoice.setTaxAmount(taxAmount);
        savedInvoice.setTotalAmount(totalAmount);

        // Save the updated invoice with totals
        Invoice finalInvoice = invoiceRepository.save(savedInvoice);

        // Send to Kafka
        kafkaTemplate.send(INVOICE_EVENTS_TOPIC, finalInvoice);

        log.info("Created invoice with ID: {} and number: {}", finalInvoice.getId(), invoiceNumber);
        return finalInvoice;
    }

    public Optional<Invoice> getInvoiceById(UUID id) {
        return invoiceRepository.findById(id);
    }

    public Optional<Invoice> getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    public Page<Invoice> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    public Page<Invoice> getInvoicesByCustomerId(UUID customerId, Pageable pageable) {
        return invoiceRepository.findByCustomerId(customerId, pageable);
    }

    public Page<Invoice> getInvoicesByStatus(Invoice.InvoiceStatus status, Pageable pageable) {
        return invoiceRepository.findByStatus(status, pageable);
    }

    public Invoice updateInvoice(UUID id, Invoice invoiceDetails) {
        log.info("Updating invoice with ID: {}", id);

        return invoiceRepository.findById(id)
                .map(invoice -> {
                    // Update fields
                    invoice.setCustomerId(invoiceDetails.getCustomerId());
                    invoice.setIssueDate(invoiceDetails.getIssueDate());
                    invoice.setDueDate(invoiceDetails.getDueDate());
                    invoice.setStatus(invoiceDetails.getStatus());
                    invoice.setCustomerName(invoiceDetails.getCustomerName());
                    invoice.setCustomerAddress(invoiceDetails.getCustomerAddress());
                    invoice.setCustomerCity(invoiceDetails.getCustomerCity());
                    invoice.setCustomerPostalCode(invoiceDetails.getCustomerPostalCode());
                    invoice.setCustomerCountry(invoiceDetails.getCustomerCountry());
                    invoice.setPaymentMethod(invoiceDetails.getPaymentMethod());
                    invoice.setPaymentDate(invoiceDetails.getPaymentDate());
                    invoice.setPaymentReference(invoiceDetails.getPaymentReference());
                    invoice.setNotes(invoiceDetails.getNotes());

                    Invoice updatedInvoice = invoiceRepository.save(invoice);

                    // Send to Kafka
                    kafkaTemplate.send(INVOICE_EVENTS_TOPIC, updatedInvoice);

                    return updatedInvoice;
                })
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + id));
    }

    public Invoice updateInvoiceStatus(UUID id, Invoice.InvoiceStatus status, String notes) {
        log.info("Updating invoice {} status to {}", id, status);

        return invoiceRepository.findById(id)
                .map(invoice -> {
                    invoice.setStatus(status);

                    if (notes != null) {
                        invoice.setNotes(notes);
                    }

                    if (status == Invoice.InvoiceStatus.SENT && invoice.getSentAt() == null) {
                        invoice.setSentAt(LocalDateTime.now());
                    }

                    Invoice updatedInvoice = invoiceRepository.save(invoice);

                    // Send to Kafka
                    kafkaTemplate.send(INVOICE_EVENTS_TOPIC, updatedInvoice);

                    return updatedInvoice;
                })
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + id));
    }

    public Invoice recordPayment(UUID id, String paymentMethod, LocalDate paymentDate, String paymentReference) {
        log.info("Recording payment for invoice {}", id);

        return invoiceRepository.findById(id)
                .map(invoice -> {
                    invoice.setStatus(Invoice.InvoiceStatus.PAID);
                    invoice.setPaymentMethod(paymentMethod);
                    invoice.setPaymentDate(paymentDate != null ? paymentDate : LocalDate.now());
                    invoice.setPaymentReference(paymentReference);

                    Invoice updatedInvoice = invoiceRepository.save(invoice);

                    // Send to Kafka
                    kafkaTemplate.send(INVOICE_EVENTS_TOPIC, updatedInvoice);

                    return updatedInvoice;
                })
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + id));
    }

    public List<Invoice> findOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now());
    }

    public List<Invoice> findInvoicesDueBetween(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findInvoicesDueBetween(startDate, endDate);
    }

    public void deleteInvoice(UUID id) {
        log.info("Deleting invoice with ID: {}", id);

        if (!invoiceRepository.existsById(id)) {
            throw new RuntimeException("Invoice not found with ID: " + id);
        }

        invoiceRepository.deleteById(id);
    }

    private String generateInvoiceNumber() {
        // Simple implementation - in a real system, this would be more sophisticated
        return "INV-" + System.currentTimeMillis();
    }
}
