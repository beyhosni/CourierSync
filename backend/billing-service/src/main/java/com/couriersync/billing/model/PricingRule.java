package com.couriersync.billing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "pricing_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    private String description;

    // Rule details
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    @Column(name = "value", nullable = false)
    private BigDecimal value;

    @Column(name = "unit")
    private String unit = "FLAT";

    // Conditions
    @Column(name = "customer_id")
    private UUID customerId; // NULL for global rules

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level")
    private PriorityLevel priorityLevel;

    @Column(name = "min_distance_km")
    private BigDecimal minDistanceKm;

    @Column(name = "max_distance_km")
    private BigDecimal maxDistanceKm;

    @Column(name = "min_weight_kg")
    private BigDecimal minWeightKg;

    @Column(name = "max_weight_kg")
    private BigDecimal maxWeightKg;

    @Column(name = "time_of_day_start")
    private LocalTime timeOfDayStart;

    @Column(name = "time_of_day_end")
    private LocalTime timeOfDayEnd;

    @Column(name = "day_of_week")
    private String dayOfWeek; // MONDAY, TUESDAY, etc. or WEEKDAY, WEEKEND

    // Status
    @Column(name = "active")
    private Boolean active = true;

    // Metadata
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    public enum RuleType {
        BASE_RATE, PER_KM_RATE, URGENT_SURCHARGE, AFTER_HOURS_SURCHARGE, 
        WEEKEND_SURCHARGE, WEIGHT_SURCHARGE, DISTANCE_SURCHARGE, CUSTOM
    }

    public enum CustomerType {
        INDIVIDUAL, BUSINESS, MEDICAL_FACILITY
    }

    public enum PriorityLevel {
        LOW, NORMAL, HIGH, URGENT
    }
}
