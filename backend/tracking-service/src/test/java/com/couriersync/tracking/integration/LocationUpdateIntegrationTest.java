package com.couriersync.tracking.integration;

import com.couriersync.tracking.model.LocationUpdate;
import com.couriersync.tracking.repository.LocationUpdateRepository;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class LocationUpdateIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LocationUpdateRepository locationUpdateRepository;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID driverId;
    private UUID deliveryId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        driverId = UUID.randomUUID();
        deliveryId = UUID.randomUUID();
    }

    @Test
    void testCreateLocationUpdate() throws Exception {
        // Given
        String requestBody = """
            {
                "driverId": "%s",
                "deliveryId": "%s",
                "latitude": "40.7128",
                "longitude": "-74.0060",
                "accuracy": "10.0",
                "speed": "30.0",
                "heading": "90.0",
                "timestamp": "%s",
                "batteryLevel": 85,
                "deviceId": "device-123"
            }
            """.formatted(driverId, deliveryId, LocalDateTime.now());

        // When & Then
        mockMvc.perform(post("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.driverId", is(driverId.toString())))
                .andExpect(jsonPath("$.deliveryId", is(deliveryId.toString())))
                .andExpect(jsonPath("$.latitude", is(40.7128)))
                .andExpect(jsonPath("$.longitude", is(-74.0060)))
                .andExpect(jsonPath("$.accuracy", is(10.0)))
                .andExpect(jsonPath("$.speed", is(30.0)))
                .andExpect(jsonPath("$.heading", is(90.0)))
                .andExpect(jsonPath("$.batteryLevel", is(85)))
                .andExpect(jsonPath("$.deviceId", is("device-123")));
    }

    @Test
    void testGetLocationUpdateById() throws Exception {
        // Given
        LocationUpdate locationUpdate = createTestLocationUpdate();
        locationUpdateRepository.save(locationUpdate);

        // When & Then
        mockMvc.perform(get("/api/locations/{id}", locationUpdate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(locationUpdate.getId().toString())))
                .andExpect(jsonPath("$.driverId", is(locationUpdate.getDriverId().toString())))
                .andExpect(jsonPath("$.deliveryId", is(locationUpdate.getDeliveryId().toString())))
                .andExpect(jsonPath("$.latitude", is(locationUpdate.getLatitude().doubleValue())))
                .andExpect(jsonPath("$.longitude", is(locationUpdate.getLongitude().doubleValue())));
    }

    @Test
    void testGetLocationUpdateByIdNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/locations/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetLocationUpdatesByDriverId() throws Exception {
        // Given
        LocationUpdate locationUpdate1 = createTestLocationUpdate();
        LocationUpdate locationUpdate2 = createTestLocationUpdate();
        locationUpdate2.setTimestamp(LocalDateTime.now().minusMinutes(10));

        locationUpdateRepository.save(locationUpdate1);
        locationUpdateRepository.save(locationUpdate2);

        // When & Then
        mockMvc.perform(get("/api/locations/driver/{driverId}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", anyOf(is(locationUpdate1.getId().toString()), is(locationUpdate2.getId().toString()))))
                .andExpect(jsonPath("$[1].id", anyOf(is(locationUpdate1.getId().toString()), is(locationUpdate2.getId().toString()))));
    }

    @Test
    void testGetLocationUpdatesByDeliveryId() throws Exception {
        // Given
        LocationUpdate locationUpdate1 = createTestLocationUpdate();
        LocationUpdate locationUpdate2 = createTestLocationUpdate();
        locationUpdate2.setTimestamp(LocalDateTime.now().minusMinutes(10));

        locationUpdateRepository.save(locationUpdate1);
        locationUpdateRepository.save(locationUpdate2);

        // When & Then
        mockMvc.perform(get("/api/locations/delivery/{deliveryId}", deliveryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", anyOf(is(locationUpdate1.getId().toString()), is(locationUpdate2.getId().toString()))))
                .andExpect(jsonPath("$[1].id", anyOf(is(locationUpdate1.getId().toString()), is(locationUpdate2.getId().toString()))));
    }

    @Test
    void testGetLatestLocationUpdateByDriverId() throws Exception {
        // Given
        LocationUpdate locationUpdate1 = createTestLocationUpdate();
        LocationUpdate locationUpdate2 = createTestLocationUpdate();
        locationUpdate2.setTimestamp(LocalDateTime.now().minusMinutes(10));

        locationUpdateRepository.save(locationUpdate1);
        locationUpdateRepository.save(locationUpdate2);

        // When & Then
        mockMvc.perform(get("/api/locations/driver/{driverId}/latest", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(locationUpdate1.getId().toString())))
                .andExpect(jsonPath("$.driverId", is(locationUpdate1.getDriverId().toString())))
                .andExpect(jsonPath("$.deliveryId", is(locationUpdate1.getDeliveryId().toString())));
    }

    @Test
    void testGetLatestLocationUpdateByDriverIdNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/locations/driver/{driverId}/latest", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetLatestLocationUpdateByDeliveryId() throws Exception {
        // Given
        LocationUpdate locationUpdate1 = createTestLocationUpdate();
        LocationUpdate locationUpdate2 = createTestLocationUpdate();
        locationUpdate2.setTimestamp(LocalDateTime.now().minusMinutes(10));

        locationUpdateRepository.save(locationUpdate1);
        locationUpdateRepository.save(locationUpdate2);

        // When & Then
        mockMvc.perform(get("/api/locations/delivery/{deliveryId}/latest", deliveryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(locationUpdate1.getId().toString())))
                .andExpect(jsonPath("$.driverId", is(locationUpdate1.getDriverId().toString())))
                .andExpect(jsonPath("$.deliveryId", is(locationUpdate1.getDeliveryId().toString())));
    }

    @Test
    void testGetLatestLocationUpdateByDeliveryIdNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/locations/delivery/{deliveryId}/latest", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateBatchLocationUpdates() throws Exception {
        // Given
        String requestBody = """
            [
                {
                    "driverId": "%s",
                    "deliveryId": "%s",
                    "latitude": "40.7128",
                    "longitude": "-74.0060",
                    "accuracy": "10.0",
                    "speed": "30.0",
                    "heading": "90.0",
                    "timestamp": "%s",
                    "batteryLevel": 85,
                    "deviceId": "device-123"
                },
                {
                    "driverId": "%s",
                    "deliveryId": "%s",
                    "latitude": "40.7580",
                    "longitude": "-73.9855",
                    "accuracy": "10.0",
                    "speed": "25.0",
                    "heading": "45.0",
                    "timestamp": "%s",
                    "batteryLevel": 84,
                    "deviceId": "device-123"
                }
            ]
            """.formatted(
                driverId, deliveryId, LocalDateTime.now(),
                driverId, deliveryId, LocalDateTime.now().plusMinutes(5)
            );

        // When & Then
        mockMvc.perform(post("/api/locations/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].driverId", is(driverId.toString())))
                .andExpect(jsonPath("$[0].deliveryId", is(deliveryId.toString())))
                .andExpect(jsonPath("$[1].driverId", is(driverId.toString())))
                .andExpect(jsonPath("$[1].deliveryId", is(deliveryId.toString())));
    }

    private LocationUpdate createTestLocationUpdate() {
        return LocationUpdate.builder()
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
    }
}
