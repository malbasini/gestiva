package com.gestiva.sales.order.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.entity.SalesOrderLine;
import com.gestiva.sales.order.repository.SalesOrderLineRepository;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class SalesOrderManageWebService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;

    public SalesOrderManageWebService(SalesOrderRepository salesOrderRepository,
                                      SalesOrderLineRepository salesOrderLineRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
    }

    public SalesOrderForm buildEditForm(Long tenantId, Long orderId) {
        SalesOrder order = salesOrderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new NotFoundException("Ordine non trovato"));

        validateEditable(order);

        List<SalesOrderLine> lines = salesOrderLineRepository
                .findByTenantIdAndSalesOrderIdOrderByLineNoAsc(tenantId, orderId);

        SalesOrderForm form = new SalesOrderForm();
        form.setCustomerId(order.getCustomerId());
        form.setQuoteId(order.getQuoteId());
        form.setOrderNumber(order.getOrderNumber());
        form.setFormattedOrderDate(PdfFormatUtils.formatDate(order.getOrderDate()));
        form.setStatus(order.getStatus());

        form.setLines(lines.stream().map(line -> {
            SalesOrderLineForm lf = new SalesOrderLineForm();
            lf.setDescription(line.getDescription());
            lf.setQuantity(line.getQuantity());
            lf.setUnitPrice(line.getUnitPrice());
            lf.setDiscountPct(defaultZero(line.getDiscountPct()));
            lf.setTaxPct(defaultZero(line.getTaxPct()));
            return lf;
        }).toList());

        if (form.getLines().isEmpty()) {
            form.getLines().add(defaultLine());
        }

        return form;
    }

    public Long update(Long tenantId, Long orderId, SalesOrderForm form) {
        SalesOrder order = salesOrderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new NotFoundException("Ordine non trovato"));

        validateEditable(order);

        order.setStatus(form.getStatus());

        List<SalesOrderLine> existingLines = salesOrderLineRepository
                .findByTenantIdAndSalesOrderIdOrderByLineNoAsc(tenantId, orderId);

        salesOrderLineRepository.deleteAll(existingLines);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;

        int lineNo = 1;
        for (SalesOrderLineForm lineForm : form.getLines()) {
            SalesOrderLine line = new SalesOrderLine();
            line.setTenantId(tenantId);
            line.setSalesOrderId(orderId);
            line.setLineNo(lineNo++);
            line.setDescription(lineForm.getDescription());
            line.setQuantity(lineForm.getQuantity());
            line.setUnitPrice(lineForm.getUnitPrice());
            line.setDiscountPct(defaultZero(lineForm.getDiscountPct()));
            line.setTaxPct(defaultZero(lineForm.getTaxPct()));

            BigDecimal gross = defaultZero(lineForm.getQuantity()).multiply(defaultZero(lineForm.getUnitPrice()));
            BigDecimal discountAmount = gross.multiply(defaultZero(lineForm.getDiscountPct()))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal taxable = gross.subtract(discountAmount);
            BigDecimal taxAmount = taxable.multiply(defaultZero(lineForm.getTaxPct()))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = taxable.add(taxAmount);

            line.setTaxAmount(taxAmount);
            line.setLineTotal(lineTotal);

            subtotal = subtotal.add(taxable);
            tax = tax.add(taxAmount);

            salesOrderLineRepository.save(line);
        }

        order.setSubtotalAmount(subtotal);
        order.setTaxAmount(tax);
        order.setTotalAmount(subtotal.add(tax));

        salesOrderRepository.save(order);
        return order.getId();
    }

    public void addLine(SalesOrderForm form) {
        form.getLines().add(defaultLine());
    }

    public void removeLine(SalesOrderForm form, int index) {
        if (form.getLines() == null || form.getLines().isEmpty()) {
            form.getLines().add(defaultLine());
            return;
        }

        if (index >= 0 && index < form.getLines().size()) {
            form.getLines().remove(index);
        }

        if (form.getLines().isEmpty()) {
            form.getLines().add(defaultLine());
        }
    }

    public SalesOrderTotalsPreviewView calculatePreviewTotals(SalesOrderForm form) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;

        if (form.getLines() != null) {
            for (SalesOrderLineForm line : form.getLines()) {
                if (line == null) {
                    continue;
                }

                BigDecimal quantity = defaultZero(line.getQuantity());
                BigDecimal unitPrice = defaultZero(line.getUnitPrice());
                BigDecimal discountPct = defaultZero(line.getDiscountPct());
                BigDecimal taxPct = defaultZero(line.getTaxPct());

                BigDecimal gross = quantity.multiply(unitPrice);
                BigDecimal discountAmount = gross.multiply(discountPct)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal taxable = gross.subtract(discountAmount);
                BigDecimal taxAmount = taxable.multiply(taxPct)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                subtotal = subtotal.add(taxable);
                tax = tax.add(taxAmount);
            }
        }

        SalesOrderTotalsPreviewView view = new SalesOrderTotalsPreviewView();
        view.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(subtotal));
        view.setFormattedTaxAmount(PdfFormatUtils.formatMoney(tax));
        view.setFormattedTotalAmount(PdfFormatUtils.formatMoney(subtotal.add(tax)));
        return view;
    }

    private void validateEditable(SalesOrder order) {
        if ("FULFILLED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new BusinessException("L'ordine non è più modificabile.");
        }
    }

    private SalesOrderLineForm defaultLine() {
        SalesOrderLineForm line = new SalesOrderLineForm();
        line.setQuantity(new BigDecimal("1.000"));
        line.setUnitPrice(BigDecimal.ZERO);
        line.setDiscountPct(BigDecimal.ZERO);
        line.setTaxPct(new BigDecimal("22.00"));
        return line;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}