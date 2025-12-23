package com.couriersync.tracking.controller;

import com.couriersync.tracking.model.DeliveryRoute;
import com.couriersync.tracking.model.LocationUpdate;
import com.couriersync.tracking.service.DeliveryRouteService;
import com.couriersync.tracking.service.LocationUpdateService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tracking API", description = "API for tracking medical deliveries")
@SecurityRequirement(name = "bearerAuth")
public class TrackingController {

    private final LocationUpdateService locationUpdateService;
    private final DeliveryRouteService deliveryRouteService;

    @PostMapping("/location")
    @Operation(summary = "Submit a location update")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Location update submitted successfully", 
                content = @Content(schema = @Schema(implementation = LocationUpdate.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<LocationUpdate> submitLocationUpdate(@Valid @RequestBody LocationUpdate locationUpdate) {
        log.info("Submitting location update for driver: {}, delivery: {}", 
                locationUpdate.getDriverId(), locationUpdate.getDeliveryId());
        LocationUpdate savedUpdate = locationUpdateService.saveLocationUpdate(locationUpdate);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUpdate);
    }

    @PostMapping("/location/batch")
    @Operation(summary = "Submit multiple location updates in batch")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Location updates submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<Void> submitLocationUpdatesBatch(@Valid @RequestBody List<LocationUpdate> locationUpdates) {
        log.info("Submitting batch of {} location updates", locationUpdates.size());
        locationUpdateService.saveLocationUpdatesBatch(locationUpdates);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/location/driver/{driverId}")
    @Operation(summary = "Get latest location updates for a driver")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location updates retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<List<LocationUpdate>> getLatestLocationUpdates(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId,
            @Parameter(description = "Maximum number of updates to return") @RequestParam(defaultValue = "10") int limit) {
        List<LocationUpdate> updates = locationUpdateService.getLatestLocationUpdates(driverId, limit);
        return ResponseEntity.ok(updates);
    }

    @GetMapping("/location/driver/{driverId}/latest")
    @Operation(summary = "Get the latest location update for a driver")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Latest location update retrieved successfully", 
                content = @Content(schema = @Schema(implementation = LocationUpdate.class))),
        @ApiResponse(responseCode = "404", description = "No location updates found for driver"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<LocationUpdate> getLatestLocationUpdate(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId) {
        LocationUpdate update = locationUpdateService.getLatestLocationUpdate(driverId);
        return update != null ? ResponseEntity.ok(update) : ResponseEntity.notFound().build();
    }

    @GetMapping("/location/delivery/{deliveryId}")
    @Operation(summary = "Get location updates for a delivery")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location updates retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<List<LocationUpdate>> getLocationUpdatesForDelivery(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId) {
        List<LocationUpdate> updates = locationUpdateService.getLocationUpdatesForDelivery(deliveryId);
        return ResponseEntity.ok(updates);
    }

    @GetMapping("/location/driver/{driverId}/coordinates")
    @Operation(summary = "Get coordinates for a driver in a time range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coordinates retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid time range"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<List<LocationUpdate>> getCoordinatesForDriverInTimeRange(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId,
            @Parameter(description = "Start time") @RequestParam String startTime,
            @Parameter(description = "End time") @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<LocationUpdate> coordinates = locationUpdateService.getCoordinatesForDriverInTimeRange(
                    driverId, start, end);
            return ResponseEntity.ok(coordinates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/route")
    @Operation(summary = "Create a new delivery route")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Route created successfully", 
                content = @Content(schema = @Schema(implementation = DeliveryRoute.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<DeliveryRoute> createRoute(
            @Parameter(description = "Delivery ID") @RequestParam UUID deliveryId,
            @Parameter(description = "Driver ID") @RequestParam UUID driverId,
            @Valid @RequestBody DeliveryRoute.Location pickupLocation,
            @Valid @RequestBody DeliveryRoute.Location dropoffLocation,
            @Parameter(description = "Estimated distance in km") @RequestParam BigDecimal estimatedDistance,
            @Parameter(description = "Estimated duration in minutes") @RequestParam Integer estimatedDuration) {
        log.info("Creating new route for delivery: {}, driver: {}", deliveryId, driverId);
        DeliveryRoute route = deliveryRouteService.createRoute(
                deliveryId, driverId, pickupLocation, dropoffLocation, 
                estimatedDistance, estimatedDuration);
        return ResponseEntity.status(HttpStatus.CREATED).body(route);
    }

    @GetMapping("/route/{routeId}")
    @Operation(summary = "Get a route by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Route found", 
                content = @Content(schema = @Schema(implementation = DeliveryRoute.class))),
        @ApiResponse(responseCode = "404", description = "Route not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<DeliveryRoute> getRoute(
            @Parameter(description = "Route ID") @PathVariable String routeId) {
        return deliveryRouteService.getRouteById(routeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/route/delivery/{deliveryId}")
    @Operation(summary = "Get routes for a delivery")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Routes retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<List<DeliveryRoute>> getRoutesByDeliveryId(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId) {
        List<DeliveryRoute> routes = deliveryRouteService.getRoutesByDeliveryId(deliveryId);
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/route/driver/{driverId}")
    @Operation(summary = "Get routes for a driver")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Routes retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<List<DeliveryRoute>> getRoutesByDriverId(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId) {
        List<DeliveryRoute> routes = deliveryRouteService.getRoutesByDriverId(driverId);
        return ResponseEntity.ok(routes);
    }

    @PutMapping("/route/{routeId}/status")
    @Operation(summary = "Update route status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Route status updated successfully", 
                content = @Content(schema = @Schema(implementation = DeliveryRoute.class))),
        @ApiResponse(responseCode = "404", description = "Route not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<DeliveryRoute> updateRouteStatus(
            @Parameter(description = "Route ID") @PathVariable String routeId,
            @Parameter(description = "New status") @RequestParam DeliveryRoute.RouteStatus status) {
        try {
            DeliveryRoute updatedRoute = deliveryRouteService.updateRouteStatus(routeId, status);
            return ResponseEntity.ok(updatedRoute);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/route/{routeId}/points")
    @Operation(summary = "Get route points for a route")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Route points retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Route not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<List<DeliveryRoute.LocationPoint>> getRoutePoints(
            @Parameter(description = "Route ID") @PathVariable String routeId) {
        try {
            // Get route by ID and extract route points
            return deliveryRouteService.getRouteById(routeId)
                    .map(route -> ResponseEntity.ok(route.getRoutePoints()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/route/delivery/{deliveryId}/points")
    @Operation(summary = "Get route points for a delivery")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Route points retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Route not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<List<DeliveryRoute.LocationPoint>> getRoutePointsByDeliveryId(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId) {
        List<DeliveryRoute.LocationPoint> routePoints = deliveryRouteService.getRoutePointsByDeliveryId(deliveryId);
        return routePoints != null ? ResponseEntity.ok(routePoints) : ResponseEntity.notFound().build();
    }

    @PutMapping("/route/{routeId}/finalize")
    @Operation(summary = "Finalize a route with actual distance and duration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Route finalized successfully", 
                content = @Content(schema = @Schema(implementation = DeliveryRoute.class))),
        @ApiResponse(responseCode = "404", description = "Route not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<DeliveryRoute> finalizeRoute(
            @Parameter(description = "Route ID") @PathVariable String routeId,
            @Parameter(description = "Actual distance in km") @RequestParam BigDecimal actualDistance,
            @Parameter(description = "Actual duration in minutes") @RequestParam Integer actualDuration) {
        try {
            DeliveryRoute finalizedRoute = deliveryRouteService.finalizeRoute(
                    routeId, actualDistance, actualDuration);
            return ResponseEntity.ok(finalizedRoute);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
