package com.couriersync.billing.service;

import com.couriersync.billing.model.PricingRule;
import com.couriersync.billing.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    private final PricingRuleRepository pricingRuleRepository;

    public PricingCalculation calculateDeliveryPrice(
            UUID customerId,
            PricingRule.CustomerType customerType,
            PricingRule.PriorityLevel priorityLevel,
            Double distanceKm,
            Double weightKg,
            LocalDateTime deliveryTime) {

        log.debug("Calculating price for delivery: customer={}, type={}, priority={}, distance={}km, weight={}kg",
                customerId, customerType, priorityLevel, distanceKm, weightKg);

        PricingCalculation calculation = new PricingCalculation();

        // Get applicable base rate
        List<PricingRule> baseRateRules = pricingRuleRepository.findApplicableRules(
                PricingRule.RuleType.BASE_RATE,
                customerId,
                customerType,
                priorityLevel,
                LocalDate.now());

        if (baseRateRules.isEmpty()) {
            // Use default base rate
            calculation.setBaseRate(new BigDecimal("15.00"));
        } else {
            // Use the most specific rule (customer-specific > customer-type > global)
            calculation.setBaseRate(baseRateRules.get(0).getValue());
        }

        // Get per-km rate
        List<PricingRule> perKmRateRules = pricingRuleRepository.findApplicableRulesByDistance(
                PricingRule.RuleType.PER_KM_RATE,
                customerId,
                customerType,
                priorityLevel,
                distanceKm,
                LocalDate.now());

        if (perKmRateRules.isEmpty()) {
            // Use default per-km rate
            calculation.setPerKmRate(new BigDecimal("1.20"));
        } else {
            // Use the most specific rule
            calculation.setPerKmRate(perKmRateRules.get(0).getValue());
        }

        // Calculate distance charge
        BigDecimal distanceCharge = calculation.getPerKmRate()
                .multiply(new BigDecimal(distanceKm))
                .setScale(2, RoundingMode.HALF_UP);
        calculation.setDistanceCharge(distanceCharge);

        // Check for priority surcharges
        if (priorityLevel == PricingRule.PriorityLevel.URGENT) {
            List<PricingRule> urgentRules = pricingRuleRepository.findApplicableRules(
                    PricingRule.RuleType.URGENT_SURCHARGE,
                    customerId,
                    customerType,
                    priorityLevel,
                    LocalDate.now());

            if (!urgentRules.isEmpty()) {
                calculation.setUrgentSurcharge(urgentRules.get(0).getValue());
            } else {
                // Use default urgent surcharge
                calculation.setUrgentSurcharge(new BigDecimal("5.00"));
            }
        }

        // Check for after-hours surcharge
        LocalTime deliveryTimeTime = deliveryTime.toLocalTime();
        boolean isAfterHours = deliveryTimeTime.isBefore(LocalTime.of(8, 0)) || 
                              deliveryTimeTime.isAfter(LocalTime.of(18, 0));

        if (isAfterHours) {
            List<PricingRule> afterHoursRules = pricingRuleRepository.findApplicableRules(
                    PricingRule.RuleType.AFTER_HOURS_SURCHARGE,
                    customerId,
                    customerType,
                    priorityLevel,
                    LocalDate.now());

            if (!afterHoursRules.isEmpty()) {
                calculation.setAfterHoursSurcharge(afterHoursRules.get(0).getValue());
            } else {
                // Use default after-hours surcharge
                calculation.setAfterHoursSurcharge(new BigDecimal("7.50"));
            }
        }

        // Check for weekend surcharge
        DayOfWeek dayOfWeek = deliveryTime.getDayOfWeek();
        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;

        if (isWeekend) {
            List<PricingRule> weekendRules = pricingRuleRepository.findApplicableRules(
                    PricingRule.RuleType.WEEKEND_SURCHARGE,
                    customerId,
                    customerType,
                    priorityLevel,
                    LocalDate.now());

            if (!weekendRules.isEmpty()) {
                calculation.setWeekendSurcharge(weekendRules.get(0).getValue());
            } else {
                // Use default weekend surcharge
                calculation.setWeekendSurcharge(new BigDecimal("10.00"));
            }
        }

        // Check for weight surcharge
        if (weightKg > 10.0) { // Example threshold
            List<PricingRule> weightRules = pricingRuleRepository.findApplicableRulesByWeight(
                    PricingRule.RuleType.WEIGHT_SURCHARGE,
                    customerId,
                    customerType,
                    priorityLevel,
                    weightKg,
                    LocalDate.now());

            if (!weightRules.isEmpty()) {
                calculation.setWeightSurcharge(weightRules.get(0).getValue());
            }
        }

        // Check for distance surcharge
        if (distanceKm > 50.0) { // Example threshold
            List<PricingRule> distanceRules = pricingRuleRepository.findApplicableRulesByDistance(
                    PricingRule.RuleType.DISTANCE_SURCHARGE,
                    customerId,
                    customerType,
                    priorityLevel,
                    distanceKm,
                    LocalDate.now());

            if (!distanceRules.isEmpty()) {
                calculation.setDistanceSurcharge(distanceRules.get(0).getValue());
            }
        }

        // Calculate subtotal
        BigDecimal subtotal = calculation.getBaseRate()
                .add(calculation.getDistanceCharge())
                .add(calculation.getUrgentSurcharge())
                .add(calculation.getAfterHoursSurcharge())
                .add(calculation.getWeekendSurcharge())
                .add(calculation.getWeightSurcharge())
                .add(calculation.getDistanceSurcharge());
        calculation.setSubtotal(subtotal);

        // Calculate tax (assuming 10% tax rate)
        BigDecimal taxRate = new BigDecimal("0.10");
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        calculation.setTaxAmount(taxAmount);

        // Calculate total
        BigDecimal total = subtotal.add(taxAmount);
        calculation.setTotal(total);

        log.debug("Calculated price: base={}, distance={}, urgent={}, afterHours={}, weekend={}, weight={}, extraDistance={}, subtotal={}, tax={}, total={}",
                calculation.getBaseRate(), calculation.getDistanceCharge(), calculation.getUrgentSurcharge(),
                calculation.getAfterHoursSurcharge(), calculation.getWeekendSurcharge(), calculation.getWeightSurcharge(),
                calculation.getDistanceSurcharge(), calculation.getSubtotal(), calculation.getTaxAmount(), calculation.getTotal());

        return calculation;
    }

    public List<PricingRule> getAllActiveRules() {
        return pricingRuleRepository.findByActive(true);
    }

    public List<PricingRule> getRulesByType(PricingRule.RuleType ruleType) {
        return pricingRuleRepository.findByRuleType(ruleType);
    }

    public List<PricingRule> getRulesByCustomer(UUID customerId) {
        return pricingRuleRepository.findByCustomerId(customerId);
    }

    public PricingRule createRule(PricingRule rule) {
        log.info("Creating new pricing rule: {}", rule.getName());
        return pricingRuleRepository.save(rule);
    }

    public PricingRule updateRule(UUID id, PricingRule ruleDetails) {
        log.info("Updating pricing rule with ID: {}", id);
        return pricingRuleRepository.findById(id)
                .map(rule -> {
                    rule.setName(ruleDetails.getName());
                    rule.setDescription(ruleDetails.getDescription());
                    rule.setRuleType(ruleDetails.getRuleType());
                    rule.setValue(ruleDetails.getValue());
                    rule.setUnit(ruleDetails.getUnit());
                    rule.setCustomerId(ruleDetails.getCustomerId());
                    rule.setCustomerType(ruleDetails.getCustomerType());
                    rule.setPriorityLevel(ruleDetails.getPriorityLevel());
                    rule.setMinDistanceKm(ruleDetails.getMinDistanceKm());
                    rule.setMaxDistanceKm(ruleDetails.getMaxDistanceKm());
                    rule.setMinWeightKg(ruleDetails.getMinWeightKg());
                    rule.setMaxWeightKg(ruleDetails.getMaxWeightKg());
                    rule.setTimeOfDayStart(ruleDetails.getTimeOfDayStart());
                    rule.setTimeOfDayEnd(ruleDetails.getTimeOfDayEnd());
                    rule.setDayOfWeek(ruleDetails.getDayOfWeek());
                    rule.setActive(ruleDetails.getActive());
                    rule.setValidFrom(ruleDetails.getValidFrom());
                    rule.setValidUntil(ruleDetails.getValidUntil());
                    return pricingRuleRepository.save(rule);
                })
                .orElseThrow(() -> new RuntimeException("Pricing rule not found with ID: " + id));
    }

    public void deleteRule(UUID id) {
        log.info("Deleting pricing rule with ID: {}", id);
        if (!pricingRuleRepository.existsById(id)) {
            throw new RuntimeException("Pricing rule not found with ID: " + id);
        }
        pricingRuleRepository.deleteById(id);
    }

    public static class PricingCalculation {
        private BigDecimal baseRate;
        private BigDecimal perKmRate;
        private BigDecimal distanceCharge;
        private BigDecimal urgentSurcharge = BigDecimal.ZERO;
        private BigDecimal afterHoursSurcharge = BigDecimal.ZERO;
        private BigDecimal weekendSurcharge = BigDecimal.ZERO;
        private BigDecimal weightSurcharge = BigDecimal.ZERO;
        private BigDecimal distanceSurcharge = BigDecimal.ZERO;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal total;

        // Getters and setters
        public BigDecimal getBaseRate() { return baseRate; }
        public void setBaseRate(BigDecimal baseRate) { this.baseRate = baseRate; }

        public BigDecimal getPerKmRate() { return perKmRate; }
        public void setPerKmRate(BigDecimal perKmRate) { this.perKmRate = perKmRate; }

        public BigDecimal getDistanceCharge() { return distanceCharge; }
        public void setDistanceCharge(BigDecimal distanceCharge) { this.distanceCharge = distanceCharge; }

        public BigDecimal getUrgentSurcharge() { return urgentSurcharge; }
        public void setUrgentSurcharge(BigDecimal urgentSurcharge) { this.urgentSurcharge = urgentSurcharge; }

        public BigDecimal getAfterHoursSurcharge() { return afterHoursSurcharge; }
        public void setAfterHoursSurcharge(BigDecimal afterHoursSurcharge) { this.afterHoursSurcharge = afterHoursSurcharge; }

        public BigDecimal getWeekendSurcharge() { return weekendSurcharge; }
        public void setWeekendSurcharge(BigDecimal weekendSurcharge) { this.weekendSurcharge = weekendSurcharge; }

        public BigDecimal getWeightSurcharge() { return weightSurcharge; }
        public void setWeightSurcharge(BigDecimal weightSurcharge) { this.weightSurcharge = weightSurcharge; }

        public BigDecimal getDistanceSurcharge() { return distanceSurcharge; }
        public void setDistanceSurcharge(BigDecimal distanceSurcharge) { this.distanceSurcharge = distanceSurcharge; }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

        public BigDecimal getTaxAmount() { return taxAmount; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
    }
}
