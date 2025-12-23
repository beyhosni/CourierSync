package com.couriersync.billing.mapper;

import com.couriersync.billing.model.PricingRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PricingRuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ruleType", source = "ruleType", qualifiedByName = "mapRuleType")
    @Mapping(target = "customerType", source = "customerType", qualifiedByName = "mapCustomerType")
    @Mapping(target = "priorityLevel", source = "priorityLevel", qualifiedByName = "mapPriorityLevel")
    PricingRule toEntity(PricingRuleDto dto);

    @Mapping(target = "ruleType", source = "ruleType", qualifiedByName = "mapRuleTypeToDto")
    @Mapping(target = "customerType", source = "customerType", qualifiedByName = "mapCustomerTypeToDto")
    @Mapping(target = "priorityLevel", source = "priorityLevel", qualifiedByName = "mapPriorityLevelToDto")
    PricingRuleDto toDto(PricingRule entity);

    List<PricingRuleDto> toDtoList(List<PricingRule> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "ruleType", source = "ruleType", qualifiedByName = "mapRuleType")
    @Mapping(target = "customerType", source = "customerType", qualifiedByName = "mapCustomerType")
    @Mapping(target = "priorityLevel", source = "priorityLevel", qualifiedByName = "mapPriorityLevel")
    void updateEntityFromDto(PricingRuleDto dto, @MappingTarget PricingRule entity);

    @Named("mapRuleType")
    default PricingRule.RuleType mapRuleType(PricingRuleDto.RuleType ruleType) {
        if (ruleType == null) {
            return null;
        }
        return PricingRule.RuleType.valueOf(ruleType.name());
    }

    @Named("mapRuleTypeToDto")
    default PricingRuleDto.RuleType mapRuleTypeToDto(PricingRule.RuleType ruleType) {
        if (ruleType == null) {
            return null;
        }
        return PricingRuleDto.RuleType.valueOf(ruleType.name());
    }

    @Named("mapCustomerType")
    default PricingRule.CustomerType mapCustomerType(PricingRuleDto.CustomerType customerType) {
        if (customerType == null) {
            return null;
        }
        return PricingRule.CustomerType.valueOf(customerType.name());
    }

    @Named("mapCustomerTypeToDto")
    default PricingRuleDto.CustomerType mapCustomerTypeToDto(PricingRule.CustomerType customerType) {
        if (customerType == null) {
            return null;
        }
        return PricingRuleDto.CustomerType.valueOf(customerType.name());
    }

    @Named("mapPriorityLevel")
    default PricingRule.PriorityLevel mapPriorityLevel(PricingRuleDto.PriorityLevel priorityLevel) {
        if (priorityLevel == null) {
            return null;
        }
        return PricingRule.PriorityLevel.valueOf(priorityLevel.name());
    }

    @Named("mapPriorityLevelToDto")
    default PricingRuleDto.PriorityLevel mapPriorityLevelToDto(PricingRule.PriorityLevel priorityLevel) {
        if (priorityLevel == null) {
            return null;
        }
        return PricingRuleDto.PriorityLevel.valueOf(priorityLevel.name());
    }

    // DTO class definition
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class PricingRuleDto {
        private java.util.UUID id;
        private String name;
        private String description;
        private RuleType ruleType;
        private java.math.BigDecimal value;
        private String unit;
        private java.util.UUID customerId;
        private CustomerType customerType;
        private PriorityLevel priorityLevel;
        private java.math.BigDecimal minDistanceKm;
        private java.math.BigDecimal maxDistanceKm;
        private java.math.BigDecimal minWeightKg;
        private java.math.BigDecimal maxWeightKg;
        private java.time.LocalTime timeOfDayStart;
        private java.time.LocalTime timeOfDayEnd;
        private String dayOfWeek;
        private Boolean active;
        private java.util.UUID createdBy;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private java.time.LocalDate validFrom;
        private java.time.LocalDate validUntil;

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
}
