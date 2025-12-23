package com.couriersync.dispatch.controller;

import com.couriersync.dispatch.model.DeliveryOrder;
import com.couriersync.dispatch.service.DeliveryOrderService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Delivery Order API", description = "API for managing medical delivery orders")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryOrderController {

    private final DeliveryOrderService deliveryOrderService;

    @PostMapping
    @Operation(summary = "Create a new delivery order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Delivery order created successfully", 
                content = @Content(schema = @Schema(implementation = DeliveryOrder.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<DeliveryOrder> createDeliveryOrder(@Valid @RequestBody DeliveryOrder deliveryOrder) {
        log.info("Creating new delivery order for customer: {}", deliveryOrder.getCustomerId());
        DeliveryOrder createdOrder = deliveryOrderService.createDeliveryOrder(deliveryOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a delivery order by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery order found", 
                content = @Content(schema = @Schema(implementation = DeliveryOrder.class))),
        @ApiResponse(responseCode = "404", description = "Delivery order not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<DeliveryOrder> getDeliveryOrder(
            @Parameter(description = "Delivery order ID") @PathVariable UUID id) {
        return deliveryOrderService.getDeliveryOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get a delivery order by order number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery order found", 
                content = @Content(schema = @Schema(implementation = DeliveryOrder.class))),
        @ApiResponse(responseCode = "404", description = "Delivery order not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<DeliveryOrder> getDeliveryOrderByNumber(
            @Parameter(description = "Delivery order number") @PathVariable String orderNumber) {
        return deliveryOrderService.getDeliveryOrderByNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all delivery orders (paginated)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery orders retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<Page<DeliveryOrder>> getAllDeliveryOrders(Pageable pageable) {
        Page<DeliveryOrder> deliveryOrders = deliveryOrderService.getAllDeliveryOrders(pageable);
        return ResponseEntity.ok(deliveryOrders);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get delivery orders by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery orders retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<Page<DeliveryOrder>> getDeliveryOrdersByStatus(
            @Parameter(description = "Delivery status") @PathVariable DeliveryOrder.Status status,
            Pageable pageable) {
        Page<DeliveryOrder> deliveryOrders = deliveryOrderService.getDeliveryOrdersByStatus(status, pageable);
        return ResponseEntity.ok(deliveryOrders);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get delivery orders by customer ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery orders retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<List<DeliveryOrder>> getDeliveryOrdersByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId) {
        List<DeliveryOrder> deliveryOrders = deliveryOrderService.getDeliveryOrdersByCustomerId(customerId);
        return ResponseEntity.ok(deliveryOrders);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a delivery order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery order updated successfully", 
                content = @Content(schema = @Schema(implementation = DeliveryOrder.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Delivery order not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<DeliveryOrder> updateDeliveryOrder(
            @Parameter(description = "Delivery order ID") @PathVariable UUID id,
            @Valid @RequestBody DeliveryOrder deliveryOrder) {
        try {
            DeliveryOrder updatedOrder = deliveryOrderService.updateDeliveryOrder(id, deliveryOrder);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign a driver to a delivery order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver assigned successfully", 
                content = @Content(schema = @Schema(implementation = DeliveryOrder.class))),
        @ApiResponse(responseCode = "404", description = "Delivery order not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<DeliveryOrder> assignDriver(
            @Parameter(description = "Delivery order ID") @PathVariable UUID id,
            @Parameter(description = "Driver ID") @RequestParam UUID driverId) {
        try {
            // Get current user ID from security context
            UUID assignedBy = getCurrentUserId();
            DeliveryOrder updatedOrder = deliveryOrderService.assignDriver(id, driverId, assignedBy);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update delivery order status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully", 
                content = @Content(schema = @Schema(implementation = DeliveryOrder.class))),
        @ApiResponse(responseCode = "404", description = "Delivery order not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<DeliveryOrder> updateDeliveryStatus(
            @Parameter(description = "Delivery order ID") @PathVariable UUID id,
            @Parameter(description = "New status") @RequestParam DeliveryOrder.Status status,
            @Parameter(description = "Status change notes") @RequestParam(required = false) String notes) {
        try {
            // Get current user ID from security context
            UUID changedBy = getCurrentUserId();

            // Get current status for history
            DeliveryOrder currentOrder = deliveryOrderService.getDeliveryOrderById(id)
                    .orElseThrow(() -> new RuntimeException("Delivery order not found"));

            DeliveryOrder updatedOrder = deliveryOrderService.updateDeliveryStatus(
                    id, currentOrder.getStatus(), status, changedBy, notes);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a delivery order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Delivery order deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Delivery order not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDeliveryOrder(
            @Parameter(description = "Delivery order ID") @PathVariable UUID id) {
        try {
            deliveryOrderService.deleteDeliveryOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Helper method to get current user ID from security context
    private UUID getCurrentUserId() {
        // In a real implementation, this would extract the user ID from the JWT token
        // For now, return a placeholder
        return UUID.fromString("12345678-1234-1234-1234-123456789012");
    }
}
