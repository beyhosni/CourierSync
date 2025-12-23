package com.couriersync.billing.controller;

import com.couriersync.billing.model.Invoice;
import com.couriersync.billing.model.InvoiceItem;
import com.couriersync.billing.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice API", description = "API for managing medical delivery invoices")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @Operation(summary = "Create a new invoice")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Invoice created successfully", 
                content = @Content(schema = @Schema(implementation = Invoice.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Invoice> createInvoice(
            @Valid @RequestBody Invoice invoice,
            @Valid @RequestBody List<InvoiceItem> items) {
        log.info("Creating new invoice for customer: {}", invoice.getCustomerId());
        Invoice createdInvoice = invoiceService.createInvoice(invoice, items);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInvoice);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an invoice by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invoice found", 
                content = @Content(schema = @Schema(implementation = Invoice.class))),
        @ApiResponse(responseCode = "404", description = "Invoice not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'DISPATCHER')")
    public ResponseEntity<Invoice> getInvoice(
            @Parameter(description = "Invoice ID") @PathVariable UUID id) {
        return invoiceService.getInvoiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{invoiceNumber}")
    @Operation(summary = "Get an invoice by invoice number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invoice found", 
                content = @Content(schema = @Schema(implementation = Invoice.class))),
        @ApiResponse(responseCode = "404", description = "Invoice not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'DISPATCHER')")
    public ResponseEntity<Invoice> getInvoiceByNumber(
            @Parameter(description = "Invoice number") @PathVariable String invoiceNumber) {
        return invoiceService.getInvoiceByNumber(invoiceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all invoices (paginated)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Page<Invoice>> getAllInvoices(Pageable pageable) {
        Page<Invoice> invoices = invoiceService.getAllInvoices(pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get invoices by customer ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'DISPATCHER')")
    public ResponseEntity<Page<Invoice>> getInvoicesByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId,
            Pageable pageable) {
        Page<Invoice> invoices = invoiceService.getInvoicesByCustomerId(customerId, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get invoices by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Page<Invoice>> getInvoicesByStatus(
            @Parameter(description = "Invoice status") @PathVariable Invoice.InvoiceStatus status,
            Pageable pageable) {
        Page<Invoice> invoices = invoiceService.getInvoicesByStatus(status, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue invoices")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overdue invoices retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<List<Invoice>> getOverdueInvoices() {
        List<Invoice> invoices = invoiceService.findOverdueInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/due-between")
    @Operation(summary = "Get invoices due between dates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<List<Invoice>> getInvoicesDueBetween(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Invoice> invoices = invoiceService.findInvoicesDueBetween(startDate, endDate);
        return ResponseEntity.ok(invoices);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an invoice")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invoice updated successfully", 
                content = @Content(schema = @Schema(implementation = Invoice.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Invoice not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Invoice> updateInvoice(
            @Parameter(description = "Invoice ID") @PathVariable UUID id,
            @Valid @RequestBody Invoice invoice) {
        try {
            Invoice updatedInvoice = invoiceService.updateInvoice(id, invoice);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update invoice status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully", 
                content = @Content(schema = @Schema(implementation = Invoice.class))),
        @ApiResponse(responseCode = "404", description = "Invoice not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Invoice> updateInvoiceStatus(
            @Parameter(description = "Invoice ID") @PathVariable UUID id,
            @Parameter(description = "New status") @RequestParam Invoice.InvoiceStatus status,
            @Parameter(description = "Status change notes") @RequestParam(required = false) String notes) {
        try {
            Invoice updatedInvoice = invoiceService.updateInvoiceStatus(id, status, notes);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/payment")
    @Operation(summary = "Record payment for an invoice")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment recorded successfully", 
                content = @Content(schema = @Schema(implementation = Invoice.class))),
        @ApiResponse(responseCode = "404", description = "Invoice not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Invoice> recordPayment(
            @Parameter(description = "Invoice ID") @PathVariable UUID id,
            @Parameter(description = "Payment method") @RequestParam String paymentMethod,
            @Parameter(description = "Payment date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
            @Parameter(description = "Payment reference") @RequestParam(required = false) String paymentReference) {
        try {
            Invoice updatedInvoice = invoiceService.recordPayment(id, paymentMethod, paymentDate, paymentReference);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an invoice")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Invoice deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Invoice not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInvoice(
            @Parameter(description = "Invoice ID") @PathVariable UUID id) {
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
