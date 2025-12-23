package com.couriersync.billing.security;

import com.couriersync.billing.model.PricingRule;
import com.couriersync.billing.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PricingAuthorizationGuard {

    private final PricingRuleRepository pricingRuleRepository;

    public boolean canAccessPricingRules() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin, finance, and dispatchers can access pricing rules
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FINANCE")) ||
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"));
    }

    public boolean canCreatePricingRule() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin can create pricing rules
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public boolean canUpdatePricingRule(UUID ruleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin can update pricing rules
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public boolean canDeletePricingRule(UUID ruleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin can delete pricing rules
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public boolean canCalculatePrice() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin, finance, and dispatchers can calculate prices
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FINANCE")) ||
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"));
    }

    public boolean canAccessCustomerSpecificRules(UUID customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and finance can access any customer-specific rules
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FINANCE"))) {
            return true;
        }

        // Dispatchers can access pricing rules for their managed customers
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            try {
                // In a real system, we would check if the dispatcher is responsible for this customer
                // For now, we'll allow access to all customer-specific rules for dispatchers
                return true; // Simplified for demonstration
            } catch (Exception e) {
                log.error("Error checking customer-specific pricing rule access", e);
                return false;
            }
        }

        return false;
    }

    public boolean canCreateCustomerSpecificRule(UUID customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin and finance can create customer-specific rules
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FINANCE"));
    }

    public boolean canUpdateCustomerSpecificRule(UUID ruleId, UUID customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin and finance can update customer-specific rules
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FINANCE"));
    }

    public boolean canDeleteCustomerSpecificRule(UUID ruleId, UUID customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin can delete customer-specific rules
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
