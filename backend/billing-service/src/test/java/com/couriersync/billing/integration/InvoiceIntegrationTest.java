package com.couriersync.billing.integration;

import com.couriersync.billing.model.Invoice;
import com.couriersync.billing.model.InvoiceItem;
import com.couriersync.billing.repository.InvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class InvoiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID customerId;
    private UUID invoiceId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        customerId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
    }

    @Test
    void testCreateInvoice() throws Exception {
        // Given
        String requestBody = """
            {
                "customerId": "%s",
                "customerName": "Customer Name",
                "customerAddress": "123 Customer St",
                "customerCity": "Customer City",
                "customerPostalCode": "12345",
                "customerCountry": "Country",
                "issueDate": "%s",
                "dueDate": "%s",
                "status": "DRAFT",
                "subtotal": "100.00",
                "taxRate": "0.10",
                "taxAmount": "10.00",
                "totalAmount": "110.00",
                "currency": "USD",
                "notes": "Invoice notes"
            }
            """.formatted(customerId, LocalDate.now(), LocalDate.now().plusDays(30));

        // When & Then
        mockMvc.perform(post("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.invoiceNumber", notNullValue()))
                .andExpect(jsonPath("$.customerId", is(customerId.toString())))
                .andExpect(jsonPath("$.customerName", is("Customer Name")))
                .andExpect(jsonPath("$.customerAddress", is("123 Customer St")))
                .andExpect(jsonPath("$.customerCity", is("Customer City")))
                .andExpect(jsonPath("$.customerPostalCode", is("12345")))
                .andExpect(jsonPath("$.customerCountry", is("Country")))
                .andExpect(jsonPath("$.issueDate", notNullValue()))
                .andExpect(jsonPath("$.dueDate", notNullValue()))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.subtotal", is(100.00)))
                .andExpect(jsonPath("$.taxRate", is(0.10)))
                .andExpect(jsonPath("$.taxAmount", is(10.00)))
                .andExpect(jsonPath("$.totalAmount", is(110.00)))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.notes", is("Invoice notes")));
    }

    @Test
    void testGetInvoiceById() throws Exception {
        // Given
        Invoice invoice = createTestInvoice();
        invoiceRepository.save(invoice);

        // When & Then
        mockMvc.perform(get("/api/invoices/{id}", invoice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(invoice.getId().toString())))
                .andExpect(jsonPath("$.invoiceNumber", is(invoice.getInvoiceNumber())))
                .andExpect(jsonPath("$.customerId", is(invoice.getCustomerId().toString())))
                .andExpect(jsonPath("$.status", is(invoice.getStatus().toString())));
    }

    @Test
    void testGetInvoiceByIdNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/invoices/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateInvoiceStatus() throws Exception {
        // Given
        Invoice invoice = createTestInvoice();
        invoiceRepository.save(invoice);

        String requestBody = """
            {
                "status": "SENT",
                "notes": "Invoice sent to customer"
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/invoices/{id}/status", invoice.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(invoice.getId().toString())))
                .andExpect(jsonPath("$.status", is("SENT")));
    }

    @Test
    void testMarkInvoiceAsPaid() throws Exception {
        // Given
        Invoice invoice = createTestInvoice();
        invoiceRepository.save(invoice);

        String requestBody = """
            {
                "paymentMethod": "Credit Card",
                "paymentDate": "%s",
                "paymentReference": "REF-12345"
            }
            """.formatted(LocalDate.now());

        // When & Then
        mockMvc.perform(put("/api/invoices/{id}/paid", invoice.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(invoice.getId().toString())))
                .andExpect(jsonPath("$.status", is("PAID")))
                .andExpect(jsonPath("$.paymentMethod", is("Credit Card")))
                .andExpect(jsonPath("$.paymentReference", is("REF-12345")));
    }

    @Test
    void testMarkInvoiceAsOverdue() throws Exception {
        // Given
        Invoice invoice = createTestInvoice();
        invoiceRepository.save(invoice);

        // When & Then
        mockMvc.perform(put("/api/invoices/{id}/overdue", invoice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(invoice.getId().toString())))
                .andExpect(jsonPath("$.status", is("OVERDUE")));
    }

    @Test
    void testCancelInvoice() throws Exception {
        // Given
        Invoice invoice = createTestInvoice();
        invoiceRepository.save(invoice);

        String requestBody = """
            {
                "reason": "Customer requested cancellation"
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/invoices/{id}/cancel", invoice.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(invoice.getId().toString())))
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.notes", is("Customer requested cancellation")));
    }

    @Test
    void testGetInvoicesByCustomerId() throws Exception {
        // Given
        Invoice invoice1 = createTestInvoice();
        Invoice invoice2 = createTestInvoice();
        invoiceRepository.save(invoice1);
        invoiceRepository.save(invoice2);

        // When & Then
        mockMvc.perform(get("/api/invoices/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", anyOf(is(invoice1.getId().toString()), is(invoice2.getId().toString()))))
                .andExpect(jsonPath("$[1].id", anyOf(is(invoice1.getId().toString()), is(invoice2.getId().toString()))));
    }

    @Test
    void testGetInvoicesByStatus() throws Exception {
        // Given
        Invoice invoice1 = createTestInvoice();
        Invoice invoice2 = createTestInvoice();
        invoiceRepository.save(invoice1);
        invoiceRepository.save(invoice2);

        // When & Then
        mockMvc.perform(get("/api/invoices/status/{status}", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", anyOf(is(invoice1.getId().toString()), is(invoice2.getId().toString()))))
                .andExpect(jsonPath("$[1].id", anyOf(is(invoice1.getId().toString()), is(invoice2.getId().toString()))));
    }

    private Invoice createTestInvoice() {
        return Invoice.builder()
                .id(invoiceId)
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8))
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
                .notes("Invoice notes")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }
}
