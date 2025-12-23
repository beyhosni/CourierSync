package com.couriersync.billing.controller;

import com.couriersync.billing.model.PricingRule;
import com.couriersync.billing.service.PricingService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pricing API", description = "API for managing delivery pricing rules")
@SecurityRequirement(name = "bearerAuth")
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate delivery price")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Price calculated successfully", 
                content = @Content(schema = @Schema(implementation = PricingService.PricingCalculation.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'DISPATCHER')")
    public ResponseEntity<PricingService.PricingCalculation> calculateDeliveryPrice(
            @Parameter(description = "Customer ID") @RequestParam(required = false) UUID customerId,
            @Parameter(description = "Customer type") @RequestParam(required = false) PricingRule.CustomerType customerType,
            @Parameter(description = "Priority level") @RequestParam(required = false) PricingRule.PriorityLevel priorityLevel,
            @Parameter(description = "Distance in km") @RequestParam Double distanceKm,
            @Parameter(description = "Weight in kg") @RequestParam(required = false) Double weightKg,
            @Parameter(description = "Delivery time") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deliveryTime) {

        // Set default values if not provided
        if (customerType == null) {
            customerType = PricingRule.CustomerType.INDIVIDUAL;
        }

        if (priorityLevel == null) {
            priorityLevel = PricingRule.PriorityLevel.NORMAL;
        }

        if (weightKm == null) {
            weightKm = 1.0;
        }

        if (deliveryTime == null) {
            deliveryTime = LocalDateTime.now();
        }

        PricingService.PricingCalculation calculation = pricingService.calculateDeliveryPrice(
                customerId, customerType, priorityLevel, distanceKm, weightKg, deliveryTime);

        return ResponseEntity.ok(calculation);
    }

    @GetMapping("/rules")
    @Operation(summary = "Get all active pricing rules")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pricing rules retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<List<PricingRule>> getAllActiveRules() {
        List<PricingRule> rules = pricingService.getAllActiveRules();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/rules/type/{ruleType}")
    @Operation(summary = "Get pricing rules by type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pricing rules retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<List<PricingRule>> getRulesByType(
            @Parameter(description = "Rule type") @PathVariable PricingRule.RuleType ruleType) {
        List<PricingRule> rules = pricingService.getRulesByType(ruleType);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/rules/customer/{customerId}")
    @Operation(summary = "Get pricing rules for a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pricing rules retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<List<PricingRule>> getRulesByCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId) {
        List<PricingRule> rules = pricingService.getRulesByCustomer(customerId);
        return ResponseEntity.ok(rules);
    }

    @PostMapping("/rules")
    @Operation(summary = "Create a new pricing rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pricing rule created successfully", 
                content = @Content(schema = @Schema(implementation = PricingRule.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PricingRule> createRule(@Valid @RequestBody PricingRule rule) {
        log.info("Creating new pricing rule: {}", rule.getName());
        PricingRule createdRule = pricingService.createRule(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
    }

    @PutMapping("/rules/{id}")
    @Operation(summary = "Update a pricing rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pricing rule updated successfully", 
                content = @Content(schema = @Schema(implementation = PricingRule.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Pricing rule not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PricingRule> updateRule(
            @Parameter(description = "Rule ID") @PathVariable UUID id,
            @Valid @RequestBody PricingRule rule) {
        try {
            PricingRule updatedRule = pricingService.updateRule(id, rule);
            return ResponseEntity.ok(updatedRule);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/rules/{id}")
    @Operation(summary = "Delete a pricing rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pricing rule deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Pricing rule not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "Rule ID") @PathVariable UUID id) {
        try {
            pricingService.deleteRule(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
