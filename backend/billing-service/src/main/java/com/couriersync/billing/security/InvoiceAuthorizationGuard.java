package com.couriersync.billing.security;

import com.couriersync.billing.model.Invoice;
import com.couriersync.billing.repository.InvoiceRepository;
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
public class InvoiceAuthorizationGuard {

    private final InvoiceRepository invoiceRepository;

    public boolean canAccessInvoice(UUID invoiceId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and finance can access any invoice
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FINANCE"))) {
            return true;
        }

        // Dispatchers can access invoices for their managed deliveries
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            try {
                Invoice invoice = invoiceRepository.findById(invoiceId)
                        .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

                // In a real system, we would check if the dispatcher is responsible for this customer
                // For now, we'll allow access to all invoices for dispatchers
                return true; // Simplified for demonstration
            } catch (Exception e) {
                log.error("Error checking invoice access", e);
                return false;
            }
        }

        return false;
    }

    public boolean canCreateInvoice() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin and finance can create invoices
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FINANCE"));
    }

    public boolean canUpdateInvoice(UUID invoiceId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Admin and finance can update any invoice
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FINANCE"))) {
            return true;
        }

        // Dispatchers can update invoice status for their managed deliveries
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DISPATCHER"))) {
            try {
                Invoice invoice = invoiceRepository.findById(invoiceId)
                        .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

                // In a real system, we would check if the dispatcher is responsible for this customer
                // For now, we'll allow status updates for dispatchers
                return true; // Simplified for demonstration
            } catch (Exception e) {
                log.error("Error checking invoice update access", e);
                return false;
            }
        }

        return false;
    }

    public boolean canDeleteInvoice(UUID invoiceId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only admin can delete invoices
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

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
}
