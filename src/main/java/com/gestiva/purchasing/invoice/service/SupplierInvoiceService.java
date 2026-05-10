package com.gestiva.purchasing.invoice.service;

import com.gestiva.accounting.due.service.PaymentDueService;
import com.gestiva.accounting.v2.journal.service.JournalAutoPostingService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class SupplierInvoiceService {

    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final SupplierInvoiceLineRepository supplierInvoiceLineRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final PaymentDueService paymentDueService;
    private final JournalAutoPostingService journalAutoPostingService;


    public SupplierInvoiceService(SupplierInvoiceRepository supplierInvoiceRepository,
                                  SupplierInvoiceLineRepository supplierInvoiceLineRepository,
                                  GoodsReceiptRepository goodsReceiptRepository,
                                  GoodsReceiptLineRepository goodsReceiptLineRepository,
                                  PurchaseOrderRepository purchaseOrderRepository,
                                  PurchaseOrderLineRepository purchaseOrderLineRepository,
                                  PaymentDueService paymentDueService,
                                  JournalAutoPostingService journalAutoPostingService) {


        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.supplierInvoiceLineRepository = supplierInvoiceLineRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.paymentDueService = paymentDueService;
        this.journalAutoPostingService = journalAutoPostingService;
    }

    public Long createFromGoodsReceipt(Long tenantId, Long goodsReceiptId) {
        var receipt = goodsReceiptRepository.findByTenantIdAndId(tenantId, goodsReceiptId)
                .orElseThrow(() -> new BusinessException("Ricezione merci non trovata."));

        if (supplierInvoiceRepository.existsByTenantIdAndGoodsReceiptId(tenantId, goodsReceiptId)) {
            throw new BusinessException("Esiste già una fattura fornitore per questa ricezione merci.");
        }

        var purchaseOrder = purchaseOrderRepository.findByTenantIdAndId(tenantId, receipt.getPurchaseOrderId())
                .orElseThrow(() -> new BusinessException("Ordine fornitore non trovato."));

        var receiptLines = goodsReceiptLineRepository.findByTenantIdAndGoodsReceiptIdOrderByLineNoAsc(tenantId, goodsReceiptId);

        if (receiptLines.isEmpty()) {
            throw new BusinessException("La ricezione merci non contiene righe.");
        }

        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setTenantId(tenantId);
        invoice.setInvoiceNumber(nextInvoiceNumber(tenantId));
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setSupplierId(receipt.getSupplierId());
        invoice.setGoodsReceiptId(receipt.getId());
        invoice.setPurchaseOrderId(receipt.getPurchaseOrderId());
        invoice.setStatus("DRAFT");
        invoice.setCurrencyCode(purchaseOrder.getCurrencyCode());
        invoice.setNotes("Fattura fornitore generata da ricezione merci " + receipt.getReceiptNumber());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        invoice.setSubtotalAmount(subtotal);
        invoice.setTaxAmount(tax);
        invoice.setTotalAmount(total);

        SupplierInvoice saved = supplierInvoiceRepository.save(invoice);

        int lineNo = 1;
        for (var receiptLine : receiptLines) {
            var poLine = purchaseOrderLineRepository.findById(receiptLine.getPurchaseOrderLineId())
                    .orElseThrow(() -> new BusinessException("Riga ordine fornitore non trovata."));

            SupplierInvoiceLine line = new SupplierInvoiceLine();
            line.setTenantId(tenantId);
            line.setSupplierInvoiceId(saved.getId());
            line.setLineNo(lineNo++);
            line.setItemId(poLine.getItemId());
            line.setDescription(poLine.getDescription());
            line.setQuantity(receiptLine.getQuantityReceived());
            line.setUnitPrice(poLine.getUnitPrice());
            line.setDiscountPct(poLine.getDiscountPct());
            line.setTaxPct(poLine.getTaxPct());

            BigDecimal lineGross = receiptLine.getQuantityReceived().multiply(poLine.getUnitPrice());
            BigDecimal discountAmount = lineGross.multiply(poLine.getDiscountPct()).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal lineSubtotal = lineGross.subtract(discountAmount).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal lineTax = lineSubtotal.multiply(poLine.getTaxPct()).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal lineTotal = lineSubtotal.add(lineTax).setScale(2, java.math.RoundingMode.HALF_UP);

            line.setLineSubtotal(lineSubtotal);
            line.setTaxAmount(lineTax);
            line.setLineTotal(lineTotal);

            supplierInvoiceLineRepository.save(line);

            subtotal = subtotal.add(lineSubtotal);
            tax = tax.add(lineTax);
            total = total.add(lineTotal);
        }

        saved.setSubtotalAmount(subtotal.setScale(2, java.math.RoundingMode.HALF_UP));
        saved.setTaxAmount(tax.setScale(2, java.math.RoundingMode.HALF_UP));
        saved.setTotalAmount(total.setScale(2, java.math.RoundingMode.HALF_UP));
        supplierInvoiceRepository.save(saved);
        paymentDueService.createPayableFromSupplierInvoice(
                tenantId,
                saved.getSupplierId(),
                saved.getInvoiceNumber(),
                saved.getInvoiceDate(),
                saved.getCurrencyCode(),
                saved.getTotalAmount(),
                saved.getId()
        );
        journalAutoPostingService.postSupplierInvoice(
                tenantId,
                saved.getInvoiceDate(),
                saved.getInvoiceNumber(),
                saved.getSubtotalAmount(),
                saved.getTaxAmount(),
                saved.getTotalAmount(),
                saved.getCurrencyCode(),
                saved.getId(),
                saved.getNotes()
        );
        return saved.getId();
    }

    private String nextInvoiceNumber(Long tenantId) {
        long next = supplierInvoiceRepository.count() + 1;
        String number = "SI-" + String.format("%05d", next);

        while (supplierInvoiceRepository.existsByTenantIdAndInvoiceNumber(tenantId, number)) {
            next++;
            number = "SI-" + String.format("%05d", next);
        }
        return number;
    }
}