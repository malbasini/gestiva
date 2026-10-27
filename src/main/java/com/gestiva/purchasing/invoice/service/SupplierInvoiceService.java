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
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.security.company.context.CompanyContext;
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
    private final DocumentSequenceService documentSequenceService;
    private final CompanyContext companyContext;


    public SupplierInvoiceService(SupplierInvoiceRepository supplierInvoiceRepository,
                                  SupplierInvoiceLineRepository supplierInvoiceLineRepository,
                                  GoodsReceiptRepository goodsReceiptRepository,
                                  GoodsReceiptLineRepository goodsReceiptLineRepository,
                                  PurchaseOrderRepository purchaseOrderRepository,
                                  PurchaseOrderLineRepository purchaseOrderLineRepository,
                                  PaymentDueService paymentDueService,
                                  JournalAutoPostingService journalAutoPostingService,
                                  DocumentSequenceService documentSequenceService,
                                  CompanyContext companyContext) {


        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.supplierInvoiceLineRepository = supplierInvoiceLineRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.paymentDueService = paymentDueService;
        this.journalAutoPostingService = journalAutoPostingService;
        this.documentSequenceService = documentSequenceService;
        this.companyContext = companyContext;
    }

    public Long createFromGoodsReceipt(Long tenantId, Long goodsReceiptId) {

        // Gestiva V2 - BLOCCO TEMPORANEO DI SICUREZZA.
        // Non rimuovere prima del completamento della migrazione
        // multi-company delle fatture fornitori e della verifica
        // delle relative scadenze e registrazioni contabili.
        if (true) {
            throw new BusinessException(
                    "Generazione fatture fornitori temporaneamente non disponibile: "
                            + "migrazione multi-company Gestiva V2 in corso."
            );
        }

        Long companyId = companyContext.requireCurrentCompanyId();

        var receipt = goodsReceiptRepository.findByTenantIdAndCompanyIdAndId(tenantId, companyId, goodsReceiptId)
                .orElseThrow(() -> new BusinessException("Ricezione merci non trovata."));

        if (supplierInvoiceRepository.existsByTenantIdAndCompanyIdAndGoodsReceiptId(tenantId, companyId, goodsReceiptId)) {
            throw new BusinessException("Esiste già una fattura fornitore per questa ricezione merci.");
        }

        var purchaseOrder = purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(tenantId, companyId, receipt.getPurchaseOrderId())
                .orElseThrow(() -> new BusinessException("Ordine fornitore non trovato."));

        if (!java.util.Objects.equals(purchaseOrder.getSupplierId(), receipt.getSupplierId())) {
            throw new BusinessException(
                    "Il fornitore del ricevimento merci non coincide con quello dell'ordine di acquisto."
            );
        }

        var receiptLines = goodsReceiptLineRepository.findByTenantIdAndGoodsReceiptIdOrderByLineNoAsc(tenantId, goodsReceiptId);

        if (receiptLines.isEmpty()) {
            throw new BusinessException("La ricezione merci non contiene righe.");
        }

        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setTenantId(tenantId);
        invoice.setCompanyId(companyId);
        invoice.setInvoiceNumber(nextInvoiceNumber(tenantId, companyId));
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
            var poLine = purchaseOrderLineRepository.findByTenantIdAndPurchaseOrderIdAndId(
                    tenantId,
                    purchaseOrder.getId(),
                    receiptLine.getPurchaseOrderLineId()
            ).orElseThrow(() -> new BusinessException("Riga ordine fornitore non trovata."));

            if (receiptLine.getQuantityReceived() == null
                    || receiptLine.getQuantityReceived().signum() <= 0) {
                throw new BusinessException(
                        "La quantità ricevuta deve essere maggiore di zero."
                );
            }

            if (poLine.getQuantity() == null
                    || poLine.getQuantity().signum() <= 0) {
                throw new BusinessException(
                        "La quantità ordinata deve essere maggiore di zero."
                );
            }

            // Gestiva V2: il ricevimento memorizza quantità base,
            // mentre l'ordine conserva quantità commerciali.
            if (poLine.getUomId() == null
                    || poLine.getConversionFactor() == null
                    || poLine.getConversionFactor().signum() <= 0) {
                throw new BusinessException(
                        "Snapshot UOM dell'ordine assente o non valido."
                );
            }

            BigDecimal expectedBaseQuantity;
            try {
                expectedBaseQuantity = poLine.getQuantity()
                        .multiply(poLine.getConversionFactor())
                        .setScale(3, java.math.RoundingMode.UNNECESSARY);
            } catch (ArithmeticException ex) {
                throw new BusinessException(
                        "Quantità ordinata non convertibile "
                        + "esattamente nell'unità base."
                );
            }

            // Il flusso attuale ammette soltanto ricevimenti completi.
            if (receiptLine.getQuantityReceived()
                    .compareTo(expectedBaseQuantity) != 0) {
                throw new BusinessException(
                        "La quantità ricevuta non corrisponde "
                        + "alla quantità ordinata convertita "
                        + "nell'unità base."
                );
            }

            if (!java.util.Objects.equals(
                    poLine.getItemId(),
                    receiptLine.getItemId())) {
                throw new BusinessException(
                        "L'articolo del ricevimento merci non coincide con quello dell'ordine di acquisto."
                );
            }

            SupplierInvoiceLine line = new SupplierInvoiceLine();
            line.setTenantId(tenantId);
            line.setSupplierInvoiceId(saved.getId());
            line.setLineNo(lineNo++);
            line.setItemId(poLine.getItemId());
            line.setDescription(poLine.getDescription());
            // La fattura utilizza la quantità commerciale
            // coerente con il prezzo unitario dell'ordine.
            line.setQuantity(poLine.getQuantity());
            line.setUnitPrice(poLine.getUnitPrice());
            line.setDiscountPct(poLine.getDiscountPct());
            line.setTaxPct(poLine.getTaxPct());

            // Ricevimento completo: conserviamo gli importi
            // commerciali già determinati nell'ordine fornitore.
            BigDecimal lineSubtotal = poLine.getLineSubtotal();
            BigDecimal lineTax = poLine.getTaxAmount();
            BigDecimal lineTotal = poLine.getLineTotal();

            if (lineSubtotal == null || lineTax == null
                    || lineTotal == null) {
                throw new BusinessException(
                        "Importi economici della riga ordine mancanti."
                );
            }

            try {
                lineSubtotal = lineSubtotal.setScale(
                        2, java.math.RoundingMode.UNNECESSARY);
                lineTax = lineTax.setScale(
                        2, java.math.RoundingMode.UNNECESSARY);
                lineTotal = lineTotal.setScale(
                        2, java.math.RoundingMode.UNNECESSARY);
            } catch (ArithmeticException ex) {
                throw new BusinessException(
                        "Importi della riga ordine non rappresentabili "
                        + "con due decimali."
                );
            }

            if (lineSubtotal.signum() < 0
                    || lineTax.signum() < 0
                    || lineTotal.compareTo(lineSubtotal.add(lineTax)) != 0
                    || lineSubtotal.precision() - lineSubtotal.scale() > 13
                    || lineTax.precision() - lineTax.scale() > 13
                    || lineTotal.precision() - lineTotal.scale() > 13) {
                throw new BusinessException(
                        "Importi economici della riga ordine non coerenti "
                        + "o non rappresentabili."
                );
            }

            // Verifica di coerenza con le regole economiche
            // dell'ordine fornitore. Gli importi originali
            // rimangono quelli utilizzati nella fattura.
            if (poLine.getUnitPrice() == null
                    || poLine.getDiscountPct() == null
                    || poLine.getTaxPct() == null
                    || poLine.getUnitPrice().signum() < 0
                    || poLine.getDiscountPct().signum() < 0
                    || poLine.getDiscountPct().compareTo(
                            new BigDecimal("100")) > 0
                    || poLine.getTaxPct().signum() < 0) {
                throw new BusinessException(
                        "Prezzo, sconto o aliquota IVA "
                        + "della riga ordine non validi."
                );
            }

            BigDecimal expectedGross = poLine.getQuantity()
                    .multiply(poLine.getUnitPrice());

            BigDecimal expectedDiscount = expectedGross
                    .multiply(poLine.getDiscountPct())
                    .divide(
                            new BigDecimal("100"),
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            BigDecimal expectedSubtotal = expectedGross
                    .subtract(expectedDiscount)
                    .setScale(
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            BigDecimal expectedTax = expectedSubtotal
                    .multiply(poLine.getTaxPct())
                    .divide(
                            new BigDecimal("100"),
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            BigDecimal expectedTotal = expectedSubtotal
                    .add(expectedTax)
                    .setScale(
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            if (lineSubtotal.compareTo(expectedSubtotal) != 0
                    || lineTax.compareTo(expectedTax) != 0
                    || lineTotal.compareTo(expectedTotal) != 0) {
                throw new BusinessException(
                        "Gli importi della riga ordine non coincidono "
                        + "con quantità, prezzo, sconto e IVA."
                );
            }

            line.setLineSubtotal(lineSubtotal);
            line.setTaxAmount(lineTax);
            line.setLineTotal(lineTotal);

            supplierInvoiceLineRepository.save(line);

            subtotal = subtotal.add(lineSubtotal);
            tax = tax.add(lineTax);
            total = total.add(lineTotal);
        }

        // Capacità massima DECIMAL(15,2) dei totali fattura.
        BigDecimal maxInvoiceAmount =
                new BigDecimal("9999999999999.99");

        if (subtotal.signum() < 0
                || tax.signum() < 0
                || total.signum() < 0
                || subtotal.compareTo(maxInvoiceAmount) > 0
                || tax.compareTo(maxInvoiceAmount) > 0
                || total.compareTo(maxInvoiceAmount) > 0
                || total.compareTo(subtotal.add(tax)) != 0) {
            throw new BusinessException(
                    "Totali fattura fornitore non coerenti "
                    + "o superiori alla capacità DECIMAL(15,2)."
            );
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

    private String nextInvoiceNumber(Long tenantId, Long companyId) {
        return documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.SUPPLIER_INVOICE
        );
    }
}