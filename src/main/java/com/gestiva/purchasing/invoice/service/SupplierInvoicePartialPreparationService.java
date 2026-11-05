package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Prepara la fatturazione di un ricevimento parziale.
 *
 * Non crea documenti, righe, movimenti contabili
 * o numeratori.
 *
 * Deve essere chiamato da un workflow transazionale
 * che mantenga il lock sull'ordine fino al commit.
 */
@Service
public class SupplierInvoicePartialPreparationService {

    private final SupplierInvoicePartialGenerationGuard guard;
    private final SupplierInvoicePartialIsolationGuard isolationGuard;
    private final CompanyContext companyContext;
    private final GoodsReceiptRepository receiptRepository;
    private final GoodsReceiptLineRepository receiptLineRepository;
    private final PurchaseOrderRepository orderRepository;
    private final SupplierInvoiceRepository invoiceRepository;

    public record Preparation(
            Long tenantId,
            Long companyId,
            GoodsReceipt receipt,
            PurchaseOrder purchaseOrder,
            List<GoodsReceiptLine> receiptLines
    ) {
    }

    public SupplierInvoicePartialPreparationService(
            SupplierInvoicePartialGenerationGuard guard,
            SupplierInvoicePartialIsolationGuard isolationGuard,
            CompanyContext companyContext,
            GoodsReceiptRepository receiptRepository,
            GoodsReceiptLineRepository receiptLineRepository,
            PurchaseOrderRepository orderRepository,
            SupplierInvoiceRepository invoiceRepository) {

        this.guard = guard;
        this.isolationGuard = isolationGuard;
        this.companyContext = companyContext;
        this.receiptRepository = receiptRepository;
        this.receiptLineRepository = receiptLineRepository;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Preparation prepare(
            Long tenantId,
            Long goodsReceiptId) {

        // Prima protezione: nessun accesso ai dati
        // se la funzionalità non è esplicitamente abilitata.
        guard.requireEnabled();
        isolationGuard.requireReadCommittedWriteTransaction();

        if (tenantId == null || tenantId <= 0
                || goodsReceiptId == null
                || goodsReceiptId <= 0) {
            throw new BusinessException(
                    "Identificativi fatturazione parziale non validi."
            );
        }

        Long companyId =
                companyContext.requireCurrentCompanyId();

        GoodsReceipt receipt = receiptRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        goodsReceiptId
                )
                .orElseThrow(() -> new BusinessException(
                        "Ricezione merci non trovata."
                ));

        if (receipt.getPurchaseOrderId() == null) {
            throw new BusinessException(
                    "Ordine collegato al ricevimento mancante."
            );
        }

        // Lock acquisito PRIMA del controllo duplicazioni
        // e prima di qualsiasi successiva scrittura.
        PurchaseOrder purchaseOrder = orderRepository
                .findLockedByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        receipt.getPurchaseOrderId()
                )
                .orElseThrow(() -> new BusinessException(
                        "Ordine fornitore non trovato."
                ));

        if (!"CONFIRMED".equalsIgnoreCase(
                purchaseOrder.getStatus())) {
            throw new BusinessException(
                    "La fatturazione richiede un ordine confermato."
            );
        }

        if (!Objects.equals(
                purchaseOrder.getSupplierId(),
                receipt.getSupplierId())) {
            throw new BusinessException(
                    "Il fornitore del ricevimento non "
                    + "coincide con quello dell'ordine."
            );
        }

        if (invoiceRepository
                .existsByTenantIdAndCompanyIdAndGoodsReceiptId(
                        tenantId,
                        companyId,
                        goodsReceiptId)) {
            throw new BusinessException(
                    "Esiste già una fattura per questa ricezione."
            );
        }

        List<GoodsReceiptLine> lines = receiptLineRepository
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        tenantId,
                        companyId,
                        goodsReceiptId
                );

        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(
                    "La ricezione merci non contiene righe."
            );
        }

        return new Preparation(
                tenantId,
                companyId,
                receipt,
                purchaseOrder,
                List.copyOf(lines)
        );
    }
}
