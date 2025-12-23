package com.couriersync.dispatch.service;

import com.couriersync.common.dto.DeliveryRequestDto;
import com.couriersync.dispatch.mapper.DeliveryOrderMapper;
import com.couriersync.dispatch.model.DeliveryOrder;
import com.couriersync.dispatch.producer.EventProducer;
import com.couriersync.dispatch.repository.DeliveryOrderRepository;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryOrderServiceTest {

    @Mock
    private DeliveryOrderRepository deliveryOrderRepository;

    @Mock
    private DeliveryOrderMapper deliveryOrderMapper;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private DeliveryOrderService deliveryOrderService;

    private DeliveryOrder deliveryOrder;
    private DeliveryRequestDto deliveryRequestDto;
    private UUID deliveryId;
    private UUID customerId;
    private UUID driverId;

    @BeforeEach
    void setUp() {
        deliveryId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        driverId = UUID.randomUUID();

        deliveryOrder = DeliveryOrder.builder()
                .id(deliveryId)
                .orderNumber("ORD-12345")
                .customerId(customerId)
                .pickupName("Pickup Location")
                .pickupAddress("123 Pickup St")
                .pickupCity("Pickup City")
                .pickupPostalCode("12345")
                .pickupLatitude(new BigDecimal("40.7128"))
                .pickupLongitude(new BigDecimal("-74.0060"))
                .pickupContactName("Pickup Contact")
                .pickupContactPhone("555-1234")
                .dropoffName("Dropoff Location")
                .dropoffAddress("456 Dropoff St")
                .dropoffCity("Dropoff City")
                .dropoffPostalCode("67890")
                .dropoffLatitude(new BigDecimal("40.7580"))
                .dropoffLongitude(new BigDecimal("-73.9855"))
                .dropoffContactName("Dropoff Contact")
                .dropoffContactPhone("555-5678")
                .priority(DeliveryOrder.Priority.NORMAL)
                .packageDescription("Medical Package")
                .packageWeight(new BigDecimal("1.5"))
                .isMedicalSpecimen(true)
                .temperatureControlled(false)
                .requestedPickupTime(LocalDateTime.now().plusHours(1))
                .status(DeliveryOrder.Status.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        deliveryRequestDto = DeliveryRequestDto.builder()
                .customerId(customerId)
                .pickupName("Pickup Location")
                .pickupAddress("123 Pickup St")
                .pickupCity("Pickup City")
                .pickupPostalCode("12345")
                .pickupLatitude(new BigDecimal("40.7128"))
                .pickupLongitude(new BigDecimal("-74.0060"))
                .pickupContactName("Pickup Contact")
                .pickupContactPhone("555-1234")
                .dropoffName("Dropoff Location")
                .dropoffAddress("456 Dropoff St")
                .dropoffCity("Dropoff City")
                .dropoffPostalCode("67890")
                .dropoffLatitude(new BigDecimal("40.7580"))
                .dropoffLongitude(new BigDecimal("-73.9855"))
                .dropoffContactName("Dropoff Contact")
                .dropoffContactPhone("555-5678")
                .priority(DeliveryRequestDto.Priority.NORMAL)
                .packageDescription("Medical Package")
                .packageWeight(new BigDecimal("1.5"))
                .isMedicalSpecimen(true)
                .temperatureControlled(false)
                .requestedPickupTime(LocalDateTime.now().plusHours(1))
                .build();
    }

    @Test
    void testCreateDeliveryOrder() {
        // Given
        when(deliveryOrderMapper.toEntity(any(DeliveryRequestDto.class))).thenReturn(deliveryOrder);
        when(deliveryOrderRepository.save(any(DeliveryOrder.class))).thenReturn(deliveryOrder);
        when(deliveryOrderMapper.toDto(any(DeliveryOrder.class))).thenReturn(deliveryRequestDto);

        // When
        DeliveryRequestDto result = deliveryOrderService.createDeliveryOrder(deliveryRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(deliveryRequestDto.getCustomerId(), result.getCustomerId());
        assertEquals(deliveryRequestDto.getPickupName(), result.getPickupName());
        assertEquals(deliveryRequestDto.getDropoffName(), result.getDropoffName());
        verify(deliveryOrderRepository).save(any(DeliveryOrder.class));
        verify(eventProducer).publishDeliveryCreatedEvent(any(UUID.class), anyString(), any(UUID.class));
    }

    @Test
    void testGetDeliveryOrderById() {
        // Given
        when(deliveryOrderRepository.findById(deliveryId)).thenReturn(Optional.of(deliveryOrder));
        when(deliveryOrderMapper.toDto(deliveryOrder)).thenReturn(deliveryRequestDto);

        // When
        Optional<DeliveryRequestDto> result = deliveryOrderService.getDeliveryOrderById(deliveryId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(deliveryRequestDto.getCustomerId(), result.get().getCustomerId());
        verify(deliveryOrderRepository).findById(deliveryId);
    }

    @Test
    void testGetDeliveryOrderByIdNotFound() {
        // Given
        when(deliveryOrderRepository.findById(deliveryId)).thenReturn(Optional.empty());

        // When
        Optional<DeliveryRequestDto> result = deliveryOrderService.getDeliveryOrderById(deliveryId);

        // Then
        assertFalse(result.isPresent());
        verify(deliveryOrderRepository).findById(deliveryId);
    }

    @Test
    void testUpdateDeliveryOrder() {
        // Given
        when(deliveryOrderRepository.findById(deliveryId)).thenReturn(Optional.of(deliveryOrder));
        when(deliveryOrderRepository.save(any(DeliveryOrder.class))).thenReturn(deliveryOrder);
        when(deliveryOrderMapper.toDto(deliveryOrder)).thenReturn(deliveryRequestDto);

        // When
        DeliveryRequestDto result = deliveryOrderService.updateDeliveryOrder(deliveryId, deliveryRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(deliveryRequestDto.getCustomerId(), result.getCustomerId());
        verify(deliveryOrderRepository).save(any(DeliveryOrder.class));
        verify(deliveryOrderMapper).updateEntityFromDto(any(DeliveryRequestDto.class), any(DeliveryOrder.class));
    }

    @Test
    void testUpdateDeliveryOrderNotFound() {
        // Given
        when(deliveryOrderRepository.findById(deliveryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> deliveryOrderService.updateDeliveryOrder(deliveryId, deliveryRequestDto));
        verify(deliveryOrderRepository).findById(deliveryId);
    }

    @Test
    void testDeleteDeliveryOrder() {
        // Given
        when(deliveryOrderRepository.findById(deliveryId)).thenReturn(Optional.of(deliveryOrder));
        doNothing().when(deliveryOrderRepository).delete(deliveryOrder);

        // When
        boolean result = deliveryOrderService.deleteDeliveryOrder(deliveryId);

        // Then
        assertTrue(result);
        verify(deliveryOrderRepository).delete(deliveryOrder);
    }

    @Test
    void testDeleteDeliveryOrderNotFound() {
        // Given
        when(deliveryOrderRepository.findById(deliveryId)).thenReturn(Optional.empty());

        // When
        boolean result = deliveryOrderService.deleteDeliveryOrder(deliveryId);

        // Then
        assertFalse(result);
        verify(deliveryOrderRepository, never()).delete(any(DeliveryOrder.class));
    }

    @Test
    void testGetAllDeliveryOrders() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<DeliveryOrder> deliveryOrders = Arrays.asList(deliveryOrder);
        Page<DeliveryOrder> deliveryOrderPage = new PageImpl<>(deliveryOrders, pageable, deliveryOrders.size());

        when(deliveryOrderRepository.findAll(pageable)).thenReturn(deliveryOrderPage);
        when(deliveryOrderMapper.toDtoList(deliveryOrders)).thenReturn(Arrays.asList(deliveryRequestDto));

        // When
        Page<DeliveryRequestDto> result = deliveryOrderService.getAllDeliveryOrders(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(deliveryRequestDto.getCustomerId(), result.getContent().get(0).getCustomerId());
        verify(deliveryOrderRepository).findAll(pageable);
    }

    @Test
    void testAssignDriver() {
        // Given
        when(deliveryOrderRepository.findById(deliveryId)).thenReturn(Optional.of(deliveryOrder));
        when(deliveryOrderRepository.save(any(DeliveryOrder.class))).thenReturn(deliveryOrder);
        when(deliveryOrderMapper.toDto(deliveryOrder)).thenReturn(deliveryRequestDto);

        // When
        DeliveryRequestDto result = deliveryOrderService.assignDriver(deliveryId, driverId, UUID.randomUUID());

        // Then
        assertNotNull(result);
        verify(deliveryOrderRepository).save(any(DeliveryOrder.class));
        verify(eventProducer).publishDeliveryAssignedEvent(any(UUID.class), anyString(), any(UUID.class), any(UUID.class));
    }

    @Test
    void testAssignDriverNotFound() {
        // Given
        when(deliveryOrderRepository.findById(deliveryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> deliveryOrderService.assignDriver(deliveryId, driverId, UUID.randomUUID()));
        verify(deliveryOrderRepository).findById(deliveryId);
    }

    @Test
    void testUpdateDeliveryStatus() {
        // Given
        when(deliveryOrderRepository.findById(deliveryId)).thenReturn(Optional.of(deliveryOrder));
        when(deliveryOrderRepository.save(any(DeliveryOrder.class))).thenReturn(deliveryOrder);
        when(deliveryOrderMapper.toDto(deliveryOrder)).thenReturn(deliveryRequestDto);

        // When
        DeliveryRequestDto result = deliveryOrderService.updateDeliveryStatus(
                deliveryId, 
                DeliveryOrder.Status.CREATED, 
                DeliveryOrder.Status.ASSIGNED, 
                UUID.randomUUID(), 
                "Status updated for test"
        );

        // Then
        assertNotNull(result);
        verify(deliveryOrderRepository).save(any(DeliveryOrder.class));
        verify(eventProducer).publishDeliveryStatusUpdatedEvent(
                any(UUID.class), 
                anyString(), 
                any(UUID.class), 
                any(UUID.class), 
                any(DeliveryEvent.DeliveryStatus.class), 
                anyString()
        );
    }

    @Test
    void testUpdateDeliveryStatusNotFound() {
        // Given
        when(deliveryOrderRepository.findById(deliveryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> deliveryOrderService.updateDeliveryStatus(
                deliveryId, 
                DeliveryOrder.Status.CREATED, 
                DeliveryOrder.Status.ASSIGNED, 
                UUID.randomUUID(), 
                "Status updated for test"
        ));
        verify(deliveryOrderRepository).findById(deliveryId);
    }
}
