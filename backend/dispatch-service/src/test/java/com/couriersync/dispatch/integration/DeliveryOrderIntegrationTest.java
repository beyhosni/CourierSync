package com.couriersync.dispatch.integration;

import com.couriersync.dispatch.model.DeliveryOrder;
import com.couriersync.dispatch.repository.DeliveryOrderRepository;
import com.couriersync.dispatch.repository.DriverRepository;
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
public class DeliveryOrderIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DeliveryOrderRepository deliveryOrderRepository;

    @Autowired
    private DriverRepository driverRepository;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID customerId;
    private UUID driverId;
    private UUID deliveryId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create test driver
        customerId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        deliveryId = UUID.randomUUID();

        // In a real test, we would create a driver entity
        // For this example, we'll proceed without creating the driver entity
    }

    @Test
    void testCreateDeliveryOrder() throws Exception {
        // Given
        String requestBody = """
            {
                "customerId": "%s",
                "pickupName": "Pickup Location",
                "pickupAddress": "123 Pickup St",
                "pickupCity": "Pickup City",
                "pickupPostalCode": "12345",
                "pickupLatitude": "40.7128",
                "pickupLongitude": "-74.0060",
                "pickupContactName": "Pickup Contact",
                "pickupContactPhone": "555-1234",
                "dropoffName": "Dropoff Location",
                "dropoffAddress": "456 Dropoff St",
                "dropoffCity": "Dropoff City",
                "dropoffPostalCode": "67890",
                "dropoffLatitude": "40.7580",
                "dropoffLongitude": "-73.9855",
                "dropoffContactName": "Dropoff Contact",
                "dropoffContactPhone": "555-5678",
                "priority": "NORMAL",
                "packageDescription": "Medical Package",
                "packageWeight": "1.5",
                "isMedicalSpecimen": true,
                "temperatureControlled": false,
                "requestedPickupTime": "%s"
            }
            """.formatted(customerId, LocalDateTime.now().plusHours(1));

        // When & Then
        mockMvc.perform(post("/api/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.orderNumber", notNullValue()))
                .andExpect(jsonPath("$.customerId", is(customerId.toString())))
                .andExpect(jsonPath("$.pickupName", is("Pickup Location")))
                .andExpect(jsonPath("$.pickupAddress", is("123 Pickup St")))
                .andExpect(jsonPath("$.pickupCity", is("Pickup City")))
                .andExpect(jsonPath("$.pickupPostalCode", is("12345")))
                .andExpect(jsonPath("$.pickupLatitude", is(40.7128)))
                .andExpect(jsonPath("$.pickupLongitude", is(-74.0060)))
                .andExpect(jsonPath("$.pickupContactName", is("Pickup Contact")))
                .andExpect(jsonPath("$.pickupContactPhone", is("555-1234")))
                .andExpect(jsonPath("$.dropoffName", is("Dropoff Location")))
                .andExpect(jsonPath("$.dropoffAddress", is("456 Dropoff St")))
                .andExpect(jsonPath("$.dropoffCity", is("Dropoff City")))
                .andExpect(jsonPath("$.dropoffPostalCode", is("67890")))
                .andExpect(jsonPath("$.dropoffLatitude", is(40.7580)))
                .andExpect(jsonPath("$.dropoffLongitude", is(-73.9855)))
                .andExpect(jsonPath("$.dropoffContactName", is("Dropoff Contact")))
                .andExpect(jsonPath("$.dropoffContactPhone", is("555-5678")))
                .andExpect(jsonPath("$.priority", is("NORMAL")))
                .andExpect(jsonPath("$.packageDescription", is("Medical Package")))
                .andExpect(jsonPath("$.packageWeight", is(1.5)))
                .andExpect(jsonPath("$.isMedicalSpecimen", is(true)))
                .andExpect(jsonPath("$.temperatureControlled", is(false)))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    void testGetDeliveryOrder() throws Exception {
        // Given
        DeliveryOrder deliveryOrder = createTestDeliveryOrder();
        deliveryOrderRepository.save(deliveryOrder);

        // When & Then
        mockMvc.perform(get("/api/deliveries/{id}", deliveryOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deliveryOrder.getId().toString())))
                .andExpect(jsonPath("$.orderNumber", is(deliveryOrder.getOrderNumber())))
                .andExpect(jsonPath("$.customerId", is(deliveryOrder.getCustomerId().toString())))
                .andExpect(jsonPath("$.status", is(deliveryOrder.getStatus().toString())));
    }

    @Test
    void testGetDeliveryOrderNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/deliveries/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateDeliveryOrder() throws Exception {
        // Given
        DeliveryOrder deliveryOrder = createTestDeliveryOrder();
        deliveryOrderRepository.save(deliveryOrder);

        String requestBody = """
            {
                "customerId": "%s",
                "pickupName": "Updated Pickup Location",
                "pickupAddress": "123 Updated Pickup St",
                "pickupCity": "Updated Pickup City",
                "pickupPostalCode": "12345",
                "pickupLatitude": "40.7128",
                "pickupLongitude": "-74.0060",
                "pickupContactName": "Updated Pickup Contact",
                "pickupContactPhone": "555-1234",
                "dropoffName": "Dropoff Location",
                "dropoffAddress": "456 Dropoff St",
                "dropoffCity": "Dropoff City",
                "dropoffPostalCode": "67890",
                "dropoffLatitude": "40.7580",
                "dropoffLongitude": "-73.9855",
                "dropoffContactName": "Dropoff Contact",
                "dropoffContactPhone": "555-5678",
                "priority": "NORMAL",
                "packageDescription": "Medical Package",
                "packageWeight": "1.5",
                "isMedicalSpecimen": true,
                "temperatureControlled": false,
                "requestedPickupTime": "%s"
            }
            """.formatted(customerId, LocalDateTime.now().plusHours(1));

        // When & Then
        mockMvc.perform(put("/api/deliveries/{id}", deliveryOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deliveryOrder.getId().toString())))
                .andExpect(jsonPath("$.pickupName", is("Updated Pickup Location")))
                .andExpect(jsonPath("$.pickupAddress", is("123 Updated Pickup St")))
                .andExpect(jsonPath("$.pickupCity", is("Updated Pickup City")));
    }

    @Test
    void testAssignDriver() throws Exception {
        // Given
        DeliveryOrder deliveryOrder = createTestDeliveryOrder();
        deliveryOrderRepository.save(deliveryOrder);

        String requestBody = """
            {
                "driverId": "%s",
                "assignedBy": "%s"
            }
            """.formatted(driverId, UUID.randomUUID());

        // When & Then
        mockMvc.perform(put("/api/deliveries/{id}/assign", deliveryOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deliveryOrder.getId().toString())))
                .andExpect(jsonPath("$.assignedDriverId", is(driverId.toString())));
    }

    @Test
    void testUpdateDeliveryStatus() throws Exception {
        // Given
        DeliveryOrder deliveryOrder = createTestDeliveryOrder();
        deliveryOrderRepository.save(deliveryOrder);

        String requestBody = """
            {
                "status": "ASSIGNED",
                "reason": "Driver assigned"
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/deliveries/{id}/status", deliveryOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deliveryOrder.getId().toString())))
                .andExpect(jsonPath("$.status", is("ASSIGNED")));
    }

    @Test
    void testGetAllDeliveries() throws Exception {
        // Given
        DeliveryOrder deliveryOrder1 = createTestDeliveryOrder();
        DeliveryOrder deliveryOrder2 = createTestDeliveryOrder();
        deliveryOrderRepository.save(deliveryOrder1);
        deliveryOrderRepository.save(deliveryOrder2);

        // When & Then
        mockMvc.perform(get("/api/deliveries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", anyOf(is(deliveryOrder1.getId().toString()), is(deliveryOrder2.getId().toString()))))
                .andExpect(jsonPath("$.content[1].id", anyOf(is(deliveryOrder1.getId().toString()), is(deliveryOrder2.getId().toString()))));
    }

    private DeliveryOrder createTestDeliveryOrder() {
        return DeliveryOrder.builder()
                .id(deliveryId)
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8))
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
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
