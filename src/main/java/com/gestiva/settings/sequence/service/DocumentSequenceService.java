package com.gestiva.settings.sequence.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.settings.sequence.entity.DocumentSequence;
import com.gestiva.settings.sequence.repository.DocumentSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DocumentSequenceService {

    public static final String QUOTE = "QUOTE";
    public static final String SALES_ORDER = "SALES_ORDER";
    public static final String DELIVERY_NOTE = "DELIVERY_NOTE";
    public static final String SALES_INVOICE = "SALES_INVOICE";
    public static final String PURCHASE_ORDER = "PURCHASE_ORDER";
    public static final String GOODS_RECEIPT = "GOODS_RECEIPT";
    public static final String SUPPLIER_INVOICE = "SUPPLIER_INVOICE";
    public static final String JOURNAL_ENTRY = "JOURNAL_ENTRY";

    private final DocumentSequenceRepository documentSequenceRepository;

    public DocumentSequenceService(DocumentSequenceRepository documentSequenceRepository) {
        this.documentSequenceRepository = documentSequenceRepository;
    }

    public String nextNumber(Long tenantId, String documentType) {
        if (tenantId == null) {
            throw new BusinessException("Tenant non valido.");
        }
        if (documentType == null || documentType.trim().isEmpty()) {
            throw new BusinessException("Tipo documento non valido.");
        }

        DocumentSequence sequence = documentSequenceRepository
                .findByTenantIdAndDocumentType(tenantId, documentType.trim())
                .orElseThrow(() -> new BusinessException(
                        "Numeratore non configurato per il tipo documento: " + documentType
                ));

        long currentNumber = sequence.getNextNumber() != null ? sequence.getNextNumber() : 1L;
        int paddingSize = sequence.getPaddingSize() != null ? sequence.getPaddingSize() : 5;
        String prefix = sequence.getPrefix() != null ? sequence.getPrefix() : "";

        String formattedNumber = prefix + leftPad(currentNumber, paddingSize);

        sequence.setNextNumber(currentNumber + 1);
        documentSequenceRepository.save(sequence);

        return formattedNumber;
    }

    public String nextQuoteNumber(Long tenantId) {
        return nextNumber(tenantId, QUOTE);
    }

    public String nextSalesOrderNumber(Long tenantId) {
        return nextNumber(tenantId, SALES_ORDER);
    }

    public String nextDeliveryNoteNumber(Long tenantId) {
        return nextNumber(tenantId, DELIVERY_NOTE);
    }

    public String nextSalesInvoiceNumber(Long tenantId) {
        return nextNumber(tenantId, SALES_INVOICE);
    }

    public String nextPurchaseOrderNumber(Long tenantId) {
        return nextNumber(tenantId, PURCHASE_ORDER);
    }

    public String nextGoodsReceiptNumber(Long tenantId) {
        return nextNumber(tenantId, GOODS_RECEIPT);
    }

    public String nextSupplierInvoiceNumber(Long tenantId) {
        return nextNumber(tenantId, SUPPLIER_INVOICE);
    }

    public String nextJournalEntryNumber(Long tenantId) {
        return nextNumber(tenantId, JOURNAL_ENTRY);
    }

    private String leftPad(long value, int paddingSize) {
        return String.format("%0" + Math.max(paddingSize, 1) + "d", value);
    }
}