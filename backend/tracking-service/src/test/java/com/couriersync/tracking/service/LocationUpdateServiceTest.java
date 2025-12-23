package com.couriersync.tracking.service;

import com.couriersync.common.dto.LocationUpdateDto;
import com.couriersync.tracking.mapper.LocationUpdateMapper;
import com.couriersync.tracking.model.LocationUpdate;
import com.couriersync.tracking.producer.EventProducer;
import com.couriersync.tracking.repository.LocationUpdateRepository;
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
class LocationUpdateServiceTest {

    @Mock
    private LocationUpdateRepository locationUpdateRepository;

    @Mock
    private LocationUpdateMapper locationUpdateMapper;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private LocationUpdateService locationUpdateService;

    private LocationUpdate locationUpdate;
    private LocationUpdateDto locationUpdateDto;
    private UUID driverId;
    private UUID deliveryId;

    @BeforeEach
    void setUp() {
        driverId = UUID.randomUUID();
        deliveryId = UUID.randomUUID();

        locationUpdate = LocationUpdate.builder()
                .id(UUID.randomUUID())
                .driverId(driverId)
                .deliveryId(deliveryId)
                .latitude(new BigDecimal("40.7128"))
                .longitude(new BigDecimal("-74.0060"))
                .accuracy(new BigDecimal("10.0"))
                .speed(new BigDecimal("30.0"))
                .heading(new BigDecimal("90.0"))
                .timestamp(LocalDateTime.now())
                .batteryLevel(85)
                .deviceId("device-123")
                .createdAt(LocalDateTime.now())
                .build();

        locationUpdateDto = LocationUpdateDto.builder()
                .driverId(driverId)
                .deliveryId(deliveryId)
                .latitude(new BigDecimal("40.7128"))
                .longitude(new BigDecimal("-74.0060"))
                .accuracy(new BigDecimal("10.0"))
                .speed(new BigDecimal("30.0"))
                .heading(new BigDecimal("90.0"))
                .timestamp(LocalDateTime.now())
                .batteryLevel(85)
                .deviceId("device-123")
                .build();
    }

    @Test
    void testCreateLocationUpdate() {
        // Given
        when(locationUpdateMapper.toEntity(any(LocationUpdateDto.class))).thenReturn(locationUpdate);
        when(locationUpdateRepository.save(any(LocationUpdate.class))).thenReturn(locationUpdate);
        when(locationUpdateMapper.toDto(any(LocationUpdate.class))).thenReturn(locationUpdateDto);

        // When
        LocationUpdateDto result = locationUpdateService.createLocationUpdate(locationUpdateDto);

        // Then
        assertNotNull(result);
        assertEquals(locationUpdateDto.getDriverId(), result.getDriverId());
        assertEquals(locationUpdateDto.getLatitude(), result.getLatitude());
        assertEquals(locationUpdateDto.getLongitude(), result.getLongitude());
        verify(locationUpdateRepository).save(any(LocationUpdate.class));
        verify(eventProducer).publishLocationUpdateEvent(
                eq(driverId), 
                eq(deliveryId), 
                eq(locationUpdateDto.getLatitude()), 
                eq(locationUpdateDto.getLongitude()),
                eq(locationUpdateDto.getAccuracy()), 
                eq(locationUpdateDto.getSpeed()), 
                eq(locationUpdateDto.getHeading()),
                eq(locationUpdateDto.getBatteryLevel()), 
                eq(locationUpdateDto.getDeviceId())
        );
    }

    @Test
    void testGetLocationUpdateById() {
        // Given
        UUID locationUpdateId = UUID.randomUUID();
        when(locationUpdateRepository.findById(locationUpdateId)).thenReturn(Optional.of(locationUpdate));
        when(locationUpdateMapper.toDto(locationUpdate)).thenReturn(locationUpdateDto);

        // When
        Optional<LocationUpdateDto> result = locationUpdateService.getLocationUpdateById(locationUpdateId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(locationUpdateDto.getDriverId(), result.get().getDriverId());
        verify(locationUpdateRepository).findById(locationUpdateId);
    }

    @Test
    void testGetLocationUpdateByIdNotFound() {
        // Given
        UUID locationUpdateId = UUID.randomUUID();
        when(locationUpdateRepository.findById(locationUpdateId)).thenReturn(Optional.empty());

        // When
        Optional<LocationUpdateDto> result = locationUpdateService.getLocationUpdateById(locationUpdateId);

        // Then
        assertFalse(result.isPresent());
        verify(locationUpdateRepository).findById(locationUpdateId);
    }

    @Test
    void testGetLocationUpdatesByDriverId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<LocationUpdate> locationUpdates = Arrays.asList(locationUpdate);
        Page<LocationUpdate> locationUpdatePage = new PageImpl<>(locationUpdates, pageable, locationUpdates.size());

        when(locationUpdateRepository.findByDriverIdOrderByTimestampDesc(driverId, pageable))
                .thenReturn(locationUpdatePage);
        when(locationUpdateMapper.toDtoList(locationUpdates)).thenReturn(Arrays.asList(locationUpdateDto));

        // When
        Page<LocationUpdateDto> result = locationUpdateService.getLocationUpdatesByDriverId(driverId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(locationUpdateDto.getDriverId(), result.getContent().get(0).getDriverId());
        verify(locationUpdateRepository).findByDriverIdOrderByTimestampDesc(driverId, pageable);
    }

    @Test
    void testGetLocationUpdatesByDeliveryId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<LocationUpdate> locationUpdates = Arrays.asList(locationUpdate);
        Page<LocationUpdate> locationUpdatePage = new PageImpl<>(locationUpdates, pageable, locationUpdates.size());

        when(locationUpdateRepository.findByDeliveryIdOrderByTimestampDesc(deliveryId, pageable))
                .thenReturn(locationUpdatePage);
        when(locationUpdateMapper.toDtoList(locationUpdates)).thenReturn(Arrays.asList(locationUpdateDto));

        // When
        Page<LocationUpdateDto> result = locationUpdateService.getLocationUpdatesByDeliveryId(deliveryId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(locationUpdateDto.getDeliveryId(), result.getContent().get(0).getDeliveryId());
        verify(locationUpdateRepository).findByDeliveryIdOrderByTimestampDesc(deliveryId, pageable);
    }

    @Test
    void testGetLatestLocationUpdateByDriverId() {
        // Given
        when(locationUpdateRepository.findFirstByDriverIdOrderByTimestampDesc(driverId))
                .thenReturn(Optional.of(locationUpdate));
        when(locationUpdateMapper.toDto(locationUpdate)).thenReturn(locationUpdateDto);

        // When
        Optional<LocationUpdateDto> result = locationUpdateService.getLatestLocationUpdateByDriverId(driverId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(locationUpdateDto.getDriverId(), result.get().getDriverId());
        verify(locationUpdateRepository).findFirstByDriverIdOrderByTimestampDesc(driverId);
    }

    @Test
    void testGetLatestLocationUpdateByDriverIdNotFound() {
        // Given
        when(locationUpdateRepository.findFirstByDriverIdOrderByTimestampDesc(driverId))
                .thenReturn(Optional.empty());

        // When
        Optional<LocationUpdateDto> result = locationUpdateService.getLatestLocationUpdateByDriverId(driverId);

        // Then
        assertFalse(result.isPresent());
        verify(locationUpdateRepository).findFirstByDriverIdOrderByTimestampDesc(driverId);
    }

    @Test
    void testGetLatestLocationUpdateByDeliveryId() {
        // Given
        when(locationUpdateRepository.findFirstByDeliveryIdOrderByTimestampDesc(deliveryId))
                .thenReturn(Optional.of(locationUpdate));
        when(locationUpdateMapper.toDto(locationUpdate)).thenReturn(locationUpdateDto);

        // When
        Optional<LocationUpdateDto> result = locationUpdateService.getLatestLocationUpdateByDeliveryId(deliveryId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(locationUpdateDto.getDeliveryId(), result.get().getDeliveryId());
        verify(locationUpdateRepository).findFirstByDeliveryIdOrderByTimestampDesc(deliveryId);
    }

    @Test
    void testGetLatestLocationUpdateByDeliveryIdNotFound() {
        // Given
        when(locationUpdateRepository.findFirstByDeliveryIdOrderByTimestampDesc(deliveryId))
                .thenReturn(Optional.empty());

        // When
        Optional<LocationUpdateDto> result = locationUpdateService.getLatestLocationUpdateByDeliveryId(deliveryId);

        // Then
        assertFalse(result.isPresent());
        verify(locationUpdateRepository).findFirstByDeliveryIdOrderByTimestampDesc(deliveryId);
    }

    @Test
    void testCreateBatchLocationUpdates() {
        // Given
        List<LocationUpdateDto> locationUpdateDtos = Arrays.asList(locationUpdateDto);
        List<LocationUpdate> locationUpdates = Arrays.asList(locationUpdate);

        when(locationUpdateMapper.toEntity(any(LocationUpdateDto.class))).thenReturn(locationUpdate);
        when(locationUpdateRepository.saveAll(anyList())).thenReturn(locationUpdates);
        when(locationUpdateMapper.toDtoList(locationUpdates)).thenReturn(locationUpdateDtos);

        // When
        List<LocationUpdateDto> result = locationUpdateService.createBatchLocationUpdates(locationUpdateDtos);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(locationUpdateDto.getDriverId(), result.get(0).getDriverId());
        verify(locationUpdateRepository).saveAll(anyList());
        verify(eventProducer).publishLocationUpdateEventBatch(anyList());
    }
}
