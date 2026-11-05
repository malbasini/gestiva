package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Costruisce un piano immutabile di fattura parziale.
 *
 * Non assegna numeratori e non persiste documenti.
 *
 * Il chiamante deve avere già eseguito la preparazione
 * transazionale e acquisito il lock dell'ordine.
 */
@Component
public class SupplierInvoicePartialDraftAssembler {

    private final PurchaseOrderLineRepository orderLines;
    private final UnitOfMeasureRepository units;
    private final ItemRepository items;
    private final SupplierInvoicePartialLinePlanner planner;

    public SupplierInvoicePartialDraftAssembler(
            PurchaseOrderLineRepository orderLines,
            UnitOfMeasureRepository units,
            ItemRepository items,
            SupplierInvoicePartialLinePlanner planner) {

        this.orderLines = orderLines;
        this.units = units;
        this.items = items;
        this.planner = planner;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SupplierInvoicePartialDraftPlan assemble(
            SupplierInvoicePartialPreparationService.Preparation preparation) {

        if (preparation == null
                || preparation.receipt() == null
                || preparation.purchaseOrder() == null
                || preparation.receiptLines() == null
                || preparation.receiptLines().isEmpty()) {
            throw new BusinessException(
                    "Preparazione fattura parziale incompleta."
            );
        }

        Long tenantId = preparation.tenantId();
        Long companyId = preparation.companyId();

        var receipt = preparation.receipt();
        var order = preparation.purchaseOrder();

        if (tenantId == null || tenantId <= 0
                || companyId == null || companyId <= 0
                || receipt.getId() == null
                || order.getId() == null
                || !Objects.equals(
                        receipt.getPurchaseOrderId(),
                        order.getId())
                || !Objects.equals(
                        receipt.getSupplierId(),
                        order.getSupplierId())) {
            throw new BusinessException(
                    "Contesto ordine/ricevimento non coerente."
            );
        }

        List<SupplierInvoicePartialDraftPlan.Line> result =
                new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        int lineNo = 1;

        for (var receiptLine : preparation.receiptLines()) {

            if (receiptLine == null
                    || receiptLine.getPurchaseOrderLineId() == null
                    || receiptLine.getPurchaseOrderLineId() <= 0
                    || receiptLine.getQuantityReceived() == null
                    || receiptLine.getQuantityReceived().signum() <= 0) {
                throw new BusinessException(
                        "Riga ricevimento parziale non valida."
                );
            }

            Long orderLineId =
                    receiptLine.getPurchaseOrderLineId();

            PurchaseOrderLine orderLine = orderLines
                    .findByTenantIdAndPurchaseOrderIdAndId(
                            tenantId,
                            order.getId(),
                            orderLineId
                    )
                    .orElseThrow(() -> new BusinessException(
                            "Riga ordine fornitore non trovata."
                    ));

            if (!Objects.equals(
                    orderLine.getItemId(),
                    receiptLine.getItemId())) {
                throw new BusinessException(
                        "Articolo del ricevimento diverso "
                        + "dall'articolo ordinato."
                );
            }

            if (orderLine.getItemId() == null
                    || orderLine.getUomId() == null
                    || orderLine.getQuantity() == null
                    || orderLine.getConversionFactor() == null
                    || orderLine.getConversionFactor().signum() <= 0) {
                throw new BusinessException(
                        "Snapshot ordine fornitore incompleto."
                );
            }

            var commercialUom = units.findByTenantIdAndId(
                    tenantId, orderLine.getUomId()
            ).orElseThrow(() -> new BusinessException(
                    "Unità commerciale non trovata."
            ));

            var item = items.findByTenantIdAndId(
                    tenantId, orderLine.getItemId()
            ).orElseThrow(() -> new BusinessException(
                    "Articolo non trovato."
            ));

            if (item.getBaseUom() == null) {
                throw new BusinessException(
                        "Unità base articolo mancante."
                );
            }

            final BigDecimal orderedBaseQuantity;

            try {
                orderedBaseQuantity = orderLine.getQuantity()
                        .multiply(orderLine.getConversionFactor())
                        .setScale(3, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException ex) {
                throw new BusinessException(
                        "Quantità ordinata non convertibile "
                        + "esattamente nell'unità base."
                );
            }

            var billable = planner.plan(
                    tenantId,
                    companyId,
                    order.getId(),
                    orderLineId,
                    receipt.getId(),
                    receiptLine.getQuantityReceived(),
                    orderedBaseQuantity,
                    orderLine.getConversionFactor(),
                    orderLine.getLineSubtotal(),
                    orderLine.getTaxAmount(),
                    orderLine.getLineTotal()
            );

            var line = new SupplierInvoicePartialDraftPlan.Line(
                    lineNo++,
                    orderLineId,
                    orderLine.getItemId(),
                    orderLine.getDescription(),
                    orderLine.getUomId(),
                    commercialUom.getCode(),
                    item.getBaseUom().getCode(),
                    orderLine.getConversionFactor(),
                    billable.commercialQuantity(),
                    orderLine.getUnitPrice(),
                    orderLine.getDiscountPct(),
                    orderLine.getTaxPct(),
                    billable.subtotal(),
                    billable.tax(),
                    billable.total()
            );

            result.add(line);

            subtotal = subtotal.add(line.subtotal());
            tax = tax.add(line.tax());
            total = total.add(line.total());
        }

        return new SupplierInvoicePartialDraftPlan(
                tenantId,
                companyId,
                receipt.getId(),
                order.getId(),
                receipt.getSupplierId(),
                order.getCurrencyCode(),
                result,
                subtotal,
                tax,
                total
        );
    }
}
