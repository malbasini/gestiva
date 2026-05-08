package com.gestiva.purchasing.order.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PurchaseOrderWebService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;

    public PurchaseOrderWebService(PurchaseOrderRepository purchaseOrderRepository,
                                   PurchaseOrderLineRepository purchaseOrderLineRepository,
                                   SupplierRepository supplierRepository,
                                   ItemRepository itemRepository,
                                   GoodsReceiptRepository goodsReceiptRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.supplierRepository = supplierRepository;
        this.itemRepository = itemRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderForm buildCreateForm() {
        PurchaseOrderForm form = new PurchaseOrderForm();
        form.setOrderDate(LocalDate.now());
        form.setExpectedDeliveryDate(LocalDate.now().plusDays(7));
        form.setStatus("DRAFT");
        form.setCurrencyCode("EUR");
        form.getLines().add(defaultLine());
        return form;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderListItemView> findAll(Long tenantId) {
        var orders = purchaseOrderRepository.findAll(
                org.springframework.data.jpa.domain.Specification
                        .where((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId)),
                Sort.by(Sort.Direction.DESC, "orderDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        List<PurchaseOrderListItemView> result = new ArrayList<>();
        for (var po : orders) {
            PurchaseOrderListItemView v = new PurchaseOrderListItemView();
            v.setId(po.getId());
            v.setOrderNumber(po.getOrderNumber());
            v.setFormattedOrderDate(PdfFormatUtils.formatDate(po.getOrderDate()));
            v.setSupplierName(
                    supplierRepository.findByTenantIdAndId(tenantId, po.getSupplierId())
                            .map(s -> s.getName())
                            .orElse("-")
            );
            v.setStatus(po.getStatus());
            v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(po.getTotalAmount()));
            result.add(v);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDetailView getDetail(Long tenantId, Long id) {
        var po = purchaseOrderRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Ordine fornitore non trovato"));

        var lines = purchaseOrderLineRepository.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(tenantId, id);

        PurchaseOrderDetailView v = new PurchaseOrderDetailView();
        v.setId(po.getId());
        v.setOrderNumber(po.getOrderNumber());
        v.setFormattedOrderDate(PdfFormatUtils.formatDate(po.getOrderDate()));
        v.setFormattedExpectedDeliveryDate(po.getExpectedDeliveryDate() != null ? PdfFormatUtils.formatDate(po.getExpectedDeliveryDate()) : "-");
        v.setSupplierName(
                supplierRepository.findByTenantIdAndId(tenantId, po.getSupplierId())
                        .map(s -> s.getName())
                        .orElse("-")
        );
        v.setStatus(po.getStatus());
        v.setCurrencyCode(po.getCurrencyCode());
        v.setNotes(po.getNotes());
        v.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(po.getSubtotalAmount()));
        v.setFormattedTaxAmount(PdfFormatUtils.formatMoney(po.getTaxAmount()));
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(po.getTotalAmount()));
        var receiptOpt = goodsReceiptRepository.findFirstByTenantIdAndPurchaseOrderId(tenantId, po.getId());
        v.setHasGoodsReceipt(receiptOpt.isPresent());
        v.setCanReceiveGoods("CONFIRMED".equalsIgnoreCase(po.getStatus()) && receiptOpt.isEmpty());
        v.setCanEdit(!"CONFIRMED".equalsIgnoreCase(po.getStatus()) && !"CANCELLED".equalsIgnoreCase(po.getStatus()));
        receiptOpt.ifPresent(receipt -> {
            v.setGoodsReceiptId(receipt.getId());
            v.setGoodsReceiptNumber(receipt.getReceiptNumber());
        });
        for (var line : lines) {
            PurchaseOrderDetailLineView lv = new PurchaseOrderDetailLineView();
            lv.setLineNo(line.getLineNo());
            lv.setDescription(line.getDescription());
            lv.setFormattedQuantity(PdfFormatUtils.formatDecimal(line.getQuantity()));
            lv.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            lv.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()) + "%");
            lv.setFormattedTaxPct(PdfFormatUtils.formatDecimal(line.getTaxPct()) + "%");
            lv.setFormattedLineTotal(PdfFormatUtils.formatMoney(line.getLineTotal()));
            v.getLines().add(lv);
        }

        return v;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderForm getForm(Long tenantId, Long id) {
        var po = purchaseOrderRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Ordine fornitore non trovato"));

        var lines = purchaseOrderLineRepository.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(tenantId, id);

        PurchaseOrderForm form = new PurchaseOrderForm();
        form.setSupplierId(po.getSupplierId());
        form.setOrderDate(po.getOrderDate());
        form.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        form.setStatus(po.getStatus());
        form.setCurrencyCode(po.getCurrencyCode());
        form.setNotes(po.getNotes());

        for (var line : lines) {
            PurchaseOrderLineForm lf = new PurchaseOrderLineForm();
            lf.setItemId(line.getItemId());
            lf.setDescription(line.getDescription());
            lf.setQuantity(line.getQuantity());
            lf.setUnitPrice(line.getUnitPrice());
            lf.setDiscountPct(line.getDiscountPct());
            lf.setTaxPct(line.getTaxPct());
            form.getLines().add(lf);
        }

        if (form.getLines().isEmpty()) {
            form.getLines().add(defaultLine());
        }

        return form;
    }

    public Long create(Long tenantId, PurchaseOrderForm form) {
        validateHeader(tenantId, form);

        String orderNumber = nextOrderNumber(tenantId);

        PurchaseOrder po = new PurchaseOrder();
        po.setTenantId(tenantId);
        po.setOrderNumber(orderNumber);
        applyHeader(po, form);

        Totals totals = calculateTotals(form.getLines());
        po.setSubtotalAmount(totals.subtotal());
        po.setTaxAmount(totals.tax());
        po.setTotalAmount(totals.total());

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        saveLines(tenantId, saved.getId(), form.getLines());

        return saved.getId();
    }

    public void update(Long tenantId, Long id, PurchaseOrderForm form) {
        var po = purchaseOrderRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Ordine fornitore non trovato"));

        if ("CONFIRMED".equalsIgnoreCase(po.getStatus()) || "CANCELLED".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException("Questo ordine fornitore non è più modificabile.");
        }

        validateHeader(tenantId, form);
        applyHeader(po, form);

        Totals totals = calculateTotals(form.getLines());
        po.setSubtotalAmount(totals.subtotal());
        po.setTaxAmount(totals.tax());
        po.setTotalAmount(totals.total());

        purchaseOrderRepository.save(po);
        purchaseOrderLineRepository.deleteByTenantIdAndPurchaseOrderId(tenantId, id);
        saveLines(tenantId, id, form.getLines());
    }

    private void validateHeader(Long tenantId, PurchaseOrderForm form) {
        if (form.getSupplierId() == null) {
            throw new BusinessException("Il fornitore è obbligatorio.");
        }

        supplierRepository.findByTenantIdAndId(tenantId, form.getSupplierId())
                .orElseThrow(() -> new BusinessException("Fornitore non valido."));

        if (form.getLines() == null || form.getLines().isEmpty()) {
            throw new BusinessException("Inserire almeno una riga.");
        }
    }

    private void applyHeader(PurchaseOrder po, PurchaseOrderForm form) {
        po.setSupplierId(form.getSupplierId());
        po.setOrderDate(form.getOrderDate());
        po.setExpectedDeliveryDate(form.getExpectedDeliveryDate());
        po.setStatus(form.getStatus());
        po.setCurrencyCode(form.getCurrencyCode());
        po.setNotes(form.getNotes());
    }

    private void saveLines(Long tenantId, Long purchaseOrderId, List<PurchaseOrderLineForm> lines) {
        int lineNo = 1;

        for (var lineForm : lines) {
            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setTenantId(tenantId);
            line.setPurchaseOrderId(purchaseOrderId);
            line.setLineNo(lineNo++);
            line.setItemId(lineForm.getItemId());
            line.setDescription(lineForm.getDescription().trim());
            line.setQuantity(scale(lineForm.getQuantity(), 3));
            line.setUnitPrice(scale(lineForm.getUnitPrice(), 2));
            line.setDiscountPct(scale(lineForm.getDiscountPct(), 2));
            line.setTaxPct(scale(lineForm.getTaxPct(), 2));

            LineTotals lt = calculateLineTotals(lineForm);
            line.setLineSubtotal(lt.subtotal());
            line.setTaxAmount(lt.tax());
            line.setLineTotal(lt.total());

            purchaseOrderLineRepository.save(line);
        }
    }

    private Totals calculateTotals(List<PurchaseOrderLineForm> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (var line : lines) {
            LineTotals lt = calculateLineTotals(line);
            subtotal = subtotal.add(lt.subtotal());
            tax = tax.add(lt.tax());
            total = total.add(lt.total());
        }

        return new Totals(scale(subtotal, 2), scale(tax, 2), scale(total, 2));
    }

    private LineTotals calculateLineTotals(PurchaseOrderLineForm line) {
        BigDecimal qty = scale(line.getQuantity(), 3);
        BigDecimal unitPrice = scale(line.getUnitPrice(), 2);
        BigDecimal discountPct = scale(line.getDiscountPct(), 2);
        BigDecimal taxPct = scale(line.getTaxPct(), 2);

        BigDecimal gross = qty.multiply(unitPrice);
        BigDecimal discountAmount = gross.multiply(discountPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal subtotal = gross.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(taxPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        return new LineTotals(subtotal, tax, total);
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value == null ? BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP)
                : value.setScale(scale, RoundingMode.HALF_UP);
    }

    private PurchaseOrderLineForm defaultLine() {
        PurchaseOrderLineForm line = new PurchaseOrderLineForm();
        line.setItemId(null);
        line.setDescription("");
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        line.setDiscountPct(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        line.setTaxPct(new BigDecimal("22.00"));
        return line;
    }

    private String nextOrderNumber(Long tenantId) {
        long next = purchaseOrderRepository.count() + 1;
        String number = "PO-" + String.format("%05d", next);

        while (purchaseOrderRepository.existsByTenantIdAndOrderNumber(tenantId, number)) {
            next++;
            number = "PO-" + String.format("%05d", next);
        }
        return number;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderLineForm buildDefaultLine() {
        PurchaseOrderLineForm line = new PurchaseOrderLineForm();
        line.setItemId(null);
        line.setDescription("");
        line.setQuantity(java.math.BigDecimal.ONE);
        line.setUnitPrice(java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP));
        line.setDiscountPct(java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP));
        line.setTaxPct(new java.math.BigDecimal("22.00"));
        return line;
    }

    private record LineTotals(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {}
    private record Totals(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {}
}