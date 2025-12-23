package com.couriersync.billing.service;

import com.couriersync.billing.mapper.InvoiceMapper;
import com.couriersync.billing.model.Invoice;
import com.couriersync.billing.model.InvoiceItem;
import com.couriersync.billing.producer.EventProducer;
import com.couriersync.billing.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private InvoiceService invoiceService;

    private Invoice invoice;
    private InvoiceItem invoiceItem;
    private UUID invoiceId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        invoiceId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        invoice = Invoice.builder()
                .id(invoiceId)
                .invoiceNumber("INV-12345")
                .customerId(customerId)
                .customerName("Customer Name")
                .customerAddress("123 Customer St")
                .customerCity("Customer City")
                .customerPostalCode("12345")
                .customerCountry("Country")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .status(Invoice.InvoiceStatus.DRAFT)
                .subtotal(new BigDecimal("100.00"))
                .taxRate(new BigDecimal("0.10"))
                .taxAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("110.00"))
                .currency("USD")
                .paymentMethod(null)
                .paymentDate(null)
                .paymentReference(null)
                .notes("Invoice notes")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        invoiceItem = InvoiceItem.builder()
                .id(UUID.randomUUID())
                .invoice(invoice)
                .itemType(InvoiceItem.ItemType.DELIVERY)
                .description("Delivery service")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .discountPercent(BigDecimal.ZERO)
                .lineTotal(new BigDecimal("100.00"))
                .deliveryId(UUID.randomUUID())
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateInvoice() {
        // Given
        List<InvoiceItem> items = Arrays.asList(invoiceItem);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceMapper.toDto(any(Invoice.class))).thenReturn(new com.couriersync.common.dto.InvoiceDto());

        // When
        com.couriersync.common.dto.InvoiceDto result = invoiceService.createInvoice(invoice, items);

        // Then
        assertNotNull(result);
        verify(invoiceRepository).save(any(Invoice.class));
        verify(eventProducer).publishInvoiceCreatedEvent(eq(invoiceId), eq("INV-12345"), eq(customerId));
    }

    @Test
    void testGetInvoiceById() {
        // Given
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toDto(invoice)).thenReturn(new com.couriersync.common.dto.InvoiceDto());

        // When
        Optional<com.couriersync.common.dto.InvoiceDto> result = invoiceService.getInvoiceById(invoiceId);

        // Then
        assertTrue(result.isPresent());
        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void testGetInvoiceByIdNotFound() {
        // Given
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // When
        Optional<com.couriersync.common.dto.InvoiceDto> result = invoiceService.getInvoiceById(invoiceId);

        // Then
        assertFalse(result.isPresent());
        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void testUpdateInvoiceStatus() {
        // Given
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceMapper.toDto(invoice)).thenReturn(new com.couriersync.common.dto.InvoiceDto());

        // When
        com.couriersync.common.dto.InvoiceDto result = invoiceService.updateInvoiceStatus(
                invoiceId, 
                Invoice.InvoiceStatus.SENT, 
                "Invoice sent to customer"
        );

        // Then
        assertNotNull(result);
        assertEquals(Invoice.InvoiceStatus.SENT, invoice.getStatus());
        verify(invoiceRepository).save(any(Invoice.class));
        verify(eventProducer).publishInvoiceStatusUpdatedEvent(
                eq(invoiceId), 
                eq("INV-12345"), 
                eq(customerId), 
                eq(com.couriersync.common.events.InvoiceEvent.InvoiceStatus.SENT), 
                eq("Invoice sent to customer")
        );
    }

    @Test
    void testUpdateInvoiceStatusNotFound() {
        // Given
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> invoiceService.updateInvoiceStatus(
                invoiceId, 
                Invoice.InvoiceStatus.SENT, 
                "Invoice sent to customer"
        ));
        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void testMarkInvoiceAsPaid() {
        // Given
        String paymentMethod = "Credit Card";
        String paymentReference = "REF-12345";
        LocalDate paymentDate = LocalDate.now();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceMapper.toDto(invoice)).thenReturn(new com.couriersync.common.dto.InvoiceDto());

        // When
        com.couriersync.common.dto.InvoiceDto result = invoiceService.markInvoiceAsPaid(
                invoiceId, 
                paymentMethod, 
                paymentDate, 
                paymentReference
        );

        // Then
        assertNotNull(result);
        assertEquals(Invoice.InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(paymentMethod, invoice.getPaymentMethod());
        assertEquals(paymentDate, invoice.getPaymentDate());
        assertEquals(paymentReference, invoice.getPaymentReference());
        verify(invoiceRepository).save(any(Invoice.class));
        verify(eventProducer).publishInvoicePaidEvent(
                eq(invoiceId), 
                eq("INV-12345"), 
                eq(customerId), 
                eq(paymentMethod), 
                eq(paymentDate), 
                eq(paymentReference)
        );
    }

    @Test
    void testMarkInvoiceAsPaidNotFound() {
        // Given
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> invoiceService.markInvoiceAsPaid(
                invoiceId, 
                "Credit Card", 
                LocalDate.now(), 
                "REF-12345"
        ));
        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void testMarkInvoiceAsOverdue() {
        // Given
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceMapper.toDto(invoice)).thenReturn(new com.couriersync.common.dto.InvoiceDto());

        // When
        com.couriersync.common.dto.InvoiceDto result = invoiceService.markInvoiceAsOverdue(invoiceId);

        // Then
        assertNotNull(result);
        assertEquals(Invoice.InvoiceStatus.OVERDUE, invoice.getStatus());
        verify(invoiceRepository).save(any(Invoice.class));
        verify(eventProducer).publishInvoiceOverdueEvent(
                eq(invoiceId), 
                eq("INV-12345"), 
                eq(customerId)
        );
    }

    @Test
    void testMarkInvoiceAsOverdueNotFound() {
        // Given
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> invoiceService.markInvoiceAsOverdue(invoiceId));
        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void testCancelInvoice() {
        // Given
        String reason = "Customer requested cancellation";
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceMapper.toDto(invoice)).thenReturn(new com.couriersync.common.dto.InvoiceDto());

        // When
        com.couriersync.common.dto.InvoiceDto result = invoiceService.cancelInvoice(invoiceId, reason);

        // Then
        assertNotNull(result);
        assertEquals(Invoice.InvoiceStatus.CANCELLED, invoice.getStatus());
        assertEquals(reason, invoice.getNotes());
        verify(invoiceRepository).save(any(Invoice.class));
        verify(eventProducer).publishInvoiceCancelledEvent(
                eq(invoiceId), 
                eq("INV-12345"), 
                eq(customerId), 
                eq(reason)
        );
    }

    @Test
    void testCancelInvoiceNotFound() {
        // Given
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> invoiceService.cancelInvoice(invoiceId, "Reason"));
        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void testGetInvoicesByCustomerId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Invoice> invoices = Arrays.asList(invoice);
        Page<Invoice> invoicePage = new PageImpl<>(invoices, pageable, invoices.size());

        when(invoiceRepository.findByCustomerIdOrderByIssueDateDesc(customerId, pageable))
                .thenReturn(invoicePage);
        when(invoiceMapper.toDtoList(invoices)).thenReturn(Arrays.asList(new com.couriersync.common.dto.InvoiceDto()));

        // When
        Page<com.couriersync.common.dto.InvoiceDto> result = invoiceService.getInvoicesByCustomerId(customerId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(invoiceRepository).findByCustomerIdOrderByIssueDateDesc(customerId, pageable);
    }

    @Test
    void testGetInvoicesByStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Invoice> invoices = Arrays.asList(invoice);
        Page<Invoice> invoicePage = new PageImpl<>(invoices, pageable, invoices.size());

        when(invoiceRepository.findByStatusOrderByIssueDateDesc(Invoice.InvoiceStatus.DRAFT, pageable))
                .thenReturn(invoicePage);
        when(invoiceMapper.toDtoList(invoices)).thenReturn(Arrays.asList(new com.couriersync.common.dto.InvoiceDto()));

        // When
        Page<com.couriersync.common.dto.InvoiceDto> result = invoiceService.getInvoicesByStatus(
                Invoice.InvoiceStatus.DRAFT, 
                pageable
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(invoiceRepository).findByStatusOrderByIssueDateDesc(Invoice.InvoiceStatus.DRAFT, pageable);
    }
}
