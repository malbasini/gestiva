package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Trasforma un piano già validato in entità JPA DRAFT.
 *
 * NON salva documenti.
 * NON assegna numeratori.
 * NON apre transazioni.
 * NON genera contabilità o scadenze.
 *
 * Il piano deve essere prodotto e utilizzato dal futuro
 * coordinatore di persistenza nella stessa transazione.
 */
@Component
public class SupplierInvoicePartialDraftEntityMapper {

    public SupplierInvoice toHeader(
            SupplierInvoicePartialDraftPlan plan,
            String invoiceNumber,
            LocalDate invoiceDate) {

        if (plan == null) {
            throw new BusinessException(
                    "Piano fattura parziale mancante."
            );
        }

        if (invoiceNumber == null
                || invoiceNumber.isBlank()
                || invoiceNumber.length() > 50) {
            throw new BusinessException(
                    "Numero fattura parziale non valido."
            );
        }

        if (invoiceDate == null) {
            throw new BusinessException(
                    "Data fattura parziale mancante."
            );
        }

        SupplierInvoice header = new SupplierInvoice();

        header.setTenantId(plan.tenantId());
        header.setCompanyId(plan.companyId());
        header.setInvoiceNumber(invoiceNumber);
        header.setInvoiceDate(invoiceDate);
        header.setSupplierId(plan.supplierId());
        header.setGoodsReceiptId(plan.goodsReceiptId());
        header.setPurchaseOrderId(plan.purchaseOrderId());
        header.setStatus("DRAFT");
        header.setCurrencyCode(plan.currencyCode());

        header.setSubtotalAmount(plan.subtotal());
        header.setTaxAmount(plan.tax());
        header.setTotalAmount(plan.total());

        return header;
    }

    public List<SupplierInvoiceLine> toLines(
            SupplierInvoicePartialDraftPlan plan,
            Long supplierInvoiceId) {

        if (plan == null) {
            throw new BusinessException(
                    "Piano fattura parziale mancante."
            );
        }

        if (supplierInvoiceId == null
                || supplierInvoiceId <= 0) {
            throw new BusinessException(
                    "Identificativo fattura parziale non valido."
            );
        }

        List<SupplierInvoiceLine> result =
                new ArrayList<>();

        for (var planned : plan.lines()) {

            SupplierInvoiceLine line =
                    new SupplierInvoiceLine();

            line.setTenantId(plan.tenantId());
            line.setSupplierInvoiceId(supplierInvoiceId);
            line.setLineNo(planned.lineNo());

            line.setItemId(planned.itemId());
            line.setDescription(planned.description());

            line.setUomId(planned.uomId());
            line.setUomCode(planned.uomCode());
            line.setBaseUomCode(planned.baseUomCode());
            line.setConversionFactor(
                    planned.conversionFactor()
            );

            line.setQuantity(
                    planned.commercialQuantity()
            );
            line.setUnitPrice(planned.unitPrice());
            line.setDiscountPct(planned.discountPct());
            line.setTaxPct(planned.taxPct());

            // Gli importi provengono dall'allocazione
            // cumulativa già validata.
            // NON ricalcolare quantità * prezzo o IVA.
            line.setLineSubtotal(planned.subtotal());
            line.setTaxAmount(planned.tax());
            line.setLineTotal(planned.total());

            result.add(line);
        }

        return List.copyOf(result);
    }
}
