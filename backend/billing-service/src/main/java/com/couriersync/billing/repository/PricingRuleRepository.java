package com.couriersync.billing.repository;

import com.couriersync.billing.model.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, UUID> {

    List<PricingRule> findByRuleType(PricingRule.RuleType ruleType);

    List<PricingRule> findByCustomerId(UUID customerId);

    List<PricingRule> findByCustomerType(PricingRule.CustomerType customerType);

    List<PricingRule> findByPriorityLevel(PricingRule.PriorityLevel priorityLevel);

    List<PricingRule> findByActive(Boolean active);

    @Query("SELECT pr FROM PricingRule pr WHERE pr.active = true AND " +
           "(pr.validFrom IS NULL OR pr.validFrom <= :currentDate) AND " +
           "(pr.validUntil IS NULL OR pr.validUntil >= :currentDate) AND " +
           "pr.ruleType = :ruleType")
    List<PricingRule> findActiveRulesByType(@Param("ruleType") PricingRule.RuleType ruleType, 
                                           @Param("currentDate") LocalDate currentDate);

    @Query("SELECT pr FROM PricingRule pr WHERE pr.active = true AND " +
           "(pr.validFrom IS NULL OR pr.validFrom <= :currentDate) AND " +
           "(pr.validUntil IS NULL OR pr.validUntil >= :currentDate) AND " +
           "pr.ruleType = :ruleType AND " +
           "(pr.customerId IS NULL OR pr.customerId = :customerId) AND " +
           "(pr.customerType IS NULL OR pr.customerType = :customerType) AND " +
           "(pr.priorityLevel IS NULL OR pr.priorityLevel = :priorityLevel)")
    List<PricingRule> findApplicableRules(@Param("ruleType") PricingRule.RuleType ruleType,
                                          @Param("customerId") UUID customerId,
                                          @Param("customerType") PricingRule.CustomerType customerType,
                                          @Param("priorityLevel") PricingRule.PriorityLevel priorityLevel,
                                          @Param("currentDate") LocalDate currentDate);

    @Query("SELECT pr FROM PricingRule pr WHERE pr.active = true AND " +
           "(pr.validFrom IS NULL OR pr.validFrom <= :currentDate) AND " +
           "(pr.validUntil IS NULL OR pr.validUntil >= :currentDate) AND " +
           "pr.ruleType = :ruleType AND " +
           "(pr.customerId IS NULL OR pr.customerId = :customerId) AND " +
           "(pr.customerType IS NULL OR pr.customerType = :customerType) AND " +
           "(pr.priorityLevel IS NULL OR pr.priorityLevel = :priorityLevel) AND " +
           "(pr.minDistanceKm IS NULL OR pr.minDistanceKm <= :distance) AND " +
           "(pr.maxDistanceKm IS NULL OR pr.maxDistanceKm >= :distance)")
    List<PricingRule> findApplicableRulesByDistance(@Param("ruleType") PricingRule.RuleType ruleType,
                                                  @Param("customerId") UUID customerId,
                                                  @Param("customerType") PricingRule.CustomerType customerType,
                                                  @Param("priorityLevel") PricingRule.PriorityLevel priorityLevel,
                                                  @Param("distance") Double distance,
                                                  @Param("currentDate") LocalDate currentDate);

    @Query("SELECT pr FROM PricingRule pr WHERE pr.active = true AND " +
           "(pr.validFrom IS NULL OR pr.validFrom <= :currentDate) AND " +
           "(pr.validUntil IS NULL OR pr.validUntil >= :currentDate) AND " +
           "pr.ruleType = :ruleType AND " +
           "(pr.customerId IS NULL OR pr.customerId = :customerId) AND " +
           "(pr.customerType IS NULL OR pr.customerType = :customerType) AND " +
           "(pr.priorityLevel IS NULL OR pr.priorityLevel = :priorityLevel) AND " +
           "(pr.minWeightKg IS NULL OR pr.minWeightKg <= :weight) AND " +
           "(pr.maxWeightKg IS NULL OR pr.maxWeightKg >= :weight)")
    List<PricingRule> findApplicableRulesByWeight(@Param("ruleType") PricingRule.RuleType ruleType,
                                                 @Param("customerId") UUID customerId,
                                                 @Param("customerType") PricingRule.CustomerType customerType,
                                                 @Param("priorityLevel") PricingRule.PriorityLevel priorityLevel,
                                                 @Param("weight") Double weight,
                                                 @Param("currentDate") LocalDate currentDate);
}
