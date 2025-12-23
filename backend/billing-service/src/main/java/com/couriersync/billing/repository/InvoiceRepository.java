package com.couriersync.billing.repository;

import com.couriersync.billing.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Invoice> findByStatus(Invoice.InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByCustomerIdAndStatus(UUID customerId, Invoice.InvoiceStatus status, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.issueDate BETWEEN :startDate AND :endDate")
    Page<Invoice> findByIssueDateBetween(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate, 
                                        Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.status = 'SENT'")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate BETWEEN :startDate AND :endDate AND i.status = 'SENT'")
    List<Invoice> findInvoicesDueBetween(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    long countByStatus(@Param("status") Invoice.InvoiceStatus status);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = :status AND i.issueDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalByStatusAndDateRange(@Param("status") Invoice.InvoiceStatus status, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
}
