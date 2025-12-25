package com.couriersync.dispatch.service;

import com.couriersync.dispatch.model.DeliveryOrder;
import com.couriersync.dispatch.model.DeliveryStatusHistory;
import com.couriersync.dispatch.repository.DeliveryOrderRepository;
import com.couriersync.dispatch.repository.DeliveryStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliveryOrderService {

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final DeliveryStatusHistoryRepository statusHistoryRepository;

    public DeliveryOrder createDeliveryOrder(DeliveryOrder deliveryOrder) {
        log.info("Creating new delivery order for customer: {}", deliveryOrder.getCustomerId());

        // Generate unique order number
        String orderNumber = generateOrderNumber();
        deliveryOrder.setOrderNumber(orderNumber);

        // Set initial status
        deliveryOrder.setStatus(DeliveryOrder.Status.CREATED);

        // Save the delivery order
        DeliveryOrder savedOrder = deliveryOrderRepository.save(deliveryOrder);

        // Create initial status history entry
        createStatusHistoryEntry(
            savedOrder.getId(), 
            null, 
            DeliveryOrder.Status.CREATED, 
            savedOrder.getCreatedBy(), 
            "Delivery order created"
        );

        log.info("Created delivery order with ID: {} and order number: {}", savedOrder.getId(), orderNumber);
        return savedOrder;
    }

    public Optional<DeliveryOrder> getDeliveryOrderById(UUID id) {
        return deliveryOrderRepository.findById(id);
    }

    public Optional<DeliveryOrder> getDeliveryOrderByNumber(String orderNumber) {
        return deliveryOrderRepository.findByOrderNumber(orderNumber);
    }

    public Page<DeliveryOrder> getAllDeliveryOrders(Pageable pageable) {
        return deliveryOrderRepository.findAll(pageable);
    }

    public Page<DeliveryOrder> getDeliveryOrdersByStatus(DeliveryOrder.Status status, Pageable pageable) {
        return deliveryOrderRepository.findByStatus(status, pageable);
    }

    public List<DeliveryOrder> getDeliveryOrdersByCustomerId(UUID customerId) {
        return deliveryOrderRepository.findByCustomerId(customerId);
    }

    public DeliveryOrder updateDeliveryOrder(UUID id, DeliveryOrder deliveryOrderDetails) {
        log.info("Updating delivery order with ID: {}", id);

        return deliveryOrderRepository.findById(id)
                .map(order -> {
                    // Update fields
                    order.setPickupName(deliveryOrderDetails.getPickupName());
                    order.setPickupAddress(deliveryOrderDetails.getPickupAddress());
                    order.setPickupCity(deliveryOrderDetails.getPickupCity());
                    order.setPickupPostalCode(deliveryOrderDetails.getPickupPostalCode());
                    order.setPickupLatitude(deliveryOrderDetails.getPickupLatitude());
                    order.setPickupLongitude(deliveryOrderDetails.getPickupLongitude());
                    order.setPickupContactName(deliveryOrderDetails.getPickupContactName());
                    order.setPickupContactPhone(deliveryOrderDetails.getPickupContactPhone());
                    order.setPickupNotes(deliveryOrderDetails.getPickupNotes());

                    order.setDropoffName(deliveryOrderDetails.getDropoffName());
                    order.setDropoffAddress(deliveryOrderDetails.getDropoffAddress());
                    order.setDropoffCity(deliveryOrderDetails.getDropoffCity());
                    order.setDropoffPostalCode(deliveryOrderDetails.getDropoffPostalCode());
                    order.setDropoffLatitude(deliveryOrderDetails.getDropoffLatitude());
                    order.setDropoffLongitude(deliveryOrderDetails.getDropoffLongitude());
                    order.setDropoffContactName(deliveryOrderDetails.getDropoffContactName());
                    order.setDropoffContactPhone(deliveryOrderDetails.getDropoffContactPhone());
                    order.setDropoffNotes(deliveryOrderDetails.getDropoffNotes());

                    order.setPriority(deliveryOrderDetails.getPriority());
                    order.setPackageDescription(deliveryOrderDetails.getPackageDescription());
                    order.setPackageWeight(deliveryOrderDetails.getPackageWeight());
                    order.setIsMedicalSpecimen(deliveryOrderDetails.getIsMedicalSpecimen());
                    order.setTemperatureControlled(deliveryOrderDetails.getTemperatureControlled());

                    order.setRequestedPickupTime(deliveryOrderDetails.getRequestedPickupTime());
                    order.setEstimatedDeliveryTime(deliveryOrderDetails.getEstimatedDeliveryTime());

                    return deliveryOrderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("Delivery order not found with ID: " + id));
    }

    public DeliveryOrder assignDriver(UUID deliveryId, UUID driverId, UUID assignedBy) {
        log.info("Assigning driver {} to delivery order {}", driverId, deliveryId);

        return deliveryOrderRepository.findById(deliveryId)
                .map(order -> {
                    // Update the order
                    order.setAssignedDriverId(driverId);
                    order.setAssignedAt(LocalDateTime.now());

                    // Update status if needed
                    if (order.getStatus() == DeliveryOrder.Status.CREATED) {
                        updateDeliveryStatus(deliveryId, DeliveryOrder.Status.CREATED, DeliveryOrder.Status.ASSIGNED, assignedBy, "Driver assigned");
                    }

                    return deliveryOrderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("Delivery order not found with ID: " + deliveryId));
    }

    public DeliveryOrder updateDeliveryStatus(UUID deliveryId, DeliveryOrder.Status fromStatus, DeliveryOrder.Status toStatus, UUID changedBy, String notes) {
        log.info("Updating delivery order {} status from {} to {}", deliveryId, fromStatus, toStatus);

        return deliveryOrderRepository.findById(deliveryId)
                .map(order -> {
                    // Update status and timestamps based on new status
                    DeliveryOrder.Status previousStatus = order.getStatus();
                    order.setStatus(toStatus);

                    switch (toStatus) {
                        case PICKED_UP:
                            order.setActualPickupTime(LocalDateTime.now());
                            break;
                        case DELIVERED:
                            order.setActualDeliveryTime(LocalDateTime.now());
                            break;
                        case CANCELLED:
                            order.setCancelledAt(LocalDateTime.now());
                            if (notes != null) {
                                order.setCancellationReason(notes);
                            }
                            break;
                    }

                    // Create status history entry
                    createStatusHistoryEntry(deliveryId, fromStatus != null ? fromStatus : previousStatus, toStatus, changedBy, notes);

                    return deliveryOrderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("Delivery order not found with ID: " + deliveryId));
    }

    public void deleteDeliveryOrder(UUID id) {
        log.info("Deleting delivery order with ID: {}", id);

        if (!deliveryOrderRepository.existsById(id)) {
            throw new RuntimeException("Delivery order not found with ID: " + id);
        }

        deliveryOrderRepository.deleteById(id);
    }

    private String generateOrderNumber() {
        // Simple implementation - in a real system, this would be more sophisticated
        return "ORD-" + System.currentTimeMillis();
    }

    private void createStatusHistoryEntry(UUID deliveryId, DeliveryOrder.Status fromStatus, 
                                     DeliveryOrder.Status toStatus, UUID changedBy, String notes) {
        // Create a new status history entry
        DeliveryStatusHistory historyEntry = DeliveryStatusHistory.builder()
                .delivery(DeliveryOrder.builder().id(deliveryId).build())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .notes(notes)
                .build();

        statusHistoryRepository.save(historyEntry);
    }
}
