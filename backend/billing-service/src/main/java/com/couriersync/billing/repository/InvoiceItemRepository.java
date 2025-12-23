package com.couriersync.billing.repository;

import com.couriersync.billing.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {

    List<InvoiceItem> findByInvoiceId(UUID invoiceId);

    List<InvoiceItem> findByDeliveryId(UUID deliveryId);

    List<InvoiceItem> findByInvoiceIdAndItemType(UUID invoiceId, InvoiceItem.ItemType itemType);

    @Query("SELECT SUM(ii.lineTotal) FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    BigDecimal sumLineTotalByInvoiceId(@Param("invoiceId") UUID invoiceId);

    @Query("SELECT COUNT(ii) FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    long countItemsByInvoiceId(@Param("invoiceId") UUID invoiceId);

    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId ORDER BY ii.createdAt ASC")
    List<InvoiceItem> findByInvoiceIdOrderByCreatedAtAsc(@Param("invoiceId") UUID invoiceId);
}
