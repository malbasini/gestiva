package com.gestiva.purchasing.order.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.common.util.NumberInputUtils;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.security.company.context.CompanyContext;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    private final DocumentSequenceService documentSequenceService;
    private final CompanyContext companyContext;



    public PurchaseOrderWebService(PurchaseOrderRepository purchaseOrderRepository,
                                   PurchaseOrderLineRepository purchaseOrderLineRepository,
                                   SupplierRepository supplierRepository,
                                   ItemRepository itemRepository,
                                   GoodsReceiptRepository goodsReceiptRepository,
                                   DocumentSequenceService documentSequenceService,
                                   CompanyContext companyContext) {

        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.supplierRepository = supplierRepository;
        this.itemRepository = itemRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.documentSequenceService = documentSequenceService;
        this.companyContext = companyContext;
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
        Long companyId = companyContext.resolveCurrentCompanyId();

        var orders = purchaseOrderRepository.findAll(
                org.springframework.data.jpa.domain.Specification
                        .where(byTenantAndCompany(tenantId, companyId)),
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
        Long companyId = companyContext.resolveCurrentCompanyId();

        var po = purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        id
                )
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
            lv.setFormattedQuantity(PdfFormatUtils.formatDecimalTrimmed(line.getQuantity(),3));
            lv.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            lv.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()) + "%");
            lv.setFormattedTaxPct(PdfFormatUtils.formatDecimalTrimmed(line.getTaxPct(),2) + "%");
            lv.setFormattedLineTotal(PdfFormatUtils.formatMoney(line.getLineTotal()));
            v.getLines().add(lv);
        }

        return v;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderForm getForm(Long tenantId, Long id) {
        Long companyId = companyContext.resolveCurrentCompanyId();

        var po = purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        id
                )
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
            lf.setQuantity(PdfFormatUtils.formatDecimalTrimmed(line.getQuantity(),3));
            lf.setUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            lf.setDiscountPct(PdfFormatUtils.formatDecimalTrimmed(line.getDiscountPct(),2));
            lf.setTaxPct(PdfFormatUtils.formatDecimalTrimmed(line.getTaxPct(),2));
            form.getLines().add(lf);
        }

        if (form.getLines().isEmpty()) {
            form.getLines().add(defaultLine());
        }

        return form;
    }

    public void validateLines(List<PurchaseOrderLineForm> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("L'ordine deve contenere almeno una riga");
        }

        for (int i = 0; i < lines.size(); i++) {
            PurchaseOrderLineForm line = lines.get(i);
            int lineNo = i + 1;

            if (line == null) {
                throw new BusinessException("La riga " + lineNo + " non è valida");
            }

            if (line.getDescription() == null || line.getDescription().isBlank()) {
                throw new BusinessException(
                    "La descrizione della riga " + lineNo + " è obbligatoria"
                );
            }

            if (line.getQuantity() == null || line.getQuantity().isBlank()) {
                throw new BusinessException(
                    "La quantità della riga " + lineNo + " è obbligatoria"
                );
            }

            if (line.getUnitPrice() == null || line.getUnitPrice().isBlank()) {
                throw new BusinessException(
                    "Il prezzo unitario della riga " + lineNo + " è obbligatorio"
                );
            }

            if (line.getDiscountPct() == null || line.getDiscountPct().isBlank()) {
                throw new BusinessException(
                    "Lo sconto della riga " + lineNo + " è obbligatorio"
                );
            }

            if (line.getTaxPct() == null || line.getTaxPct().isBlank()) {
                throw new BusinessException(
                    "L'IVA della riga " + lineNo + " è obbligatoria"
                );
            }

            BigDecimal quantity = parseOrderQuantity(
                line.getQuantity()
            );

            BigDecimal unitPrice = NumberInputUtils.parseDecimal(
                line.getUnitPrice(), "prezzo unitario"
            );

            BigDecimal discountPct = NumberInputUtils.parseDecimal(
                line.getDiscountPct(), "sconto"
            );

            BigDecimal taxPct = NumberInputUtils.parseDecimal(
                line.getTaxPct(), "IVA"
            );

            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(
                    "La quantità della riga " + lineNo + " deve essere maggiore di zero"
                );
            }

            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(
                    "Il prezzo unitario della riga " + lineNo + " non può essere negativo"
                );
            }

            if (unitPrice.stripTrailingZeros().scale() > 2) {
                throw new BusinessException(
                    "Il prezzo unitario della riga " + lineNo +
                    " può avere al massimo 2 decimali"
                );
            }

            if (discountPct.compareTo(BigDecimal.ZERO) < 0 ||
                discountPct.compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException(
                    "Lo sconto della riga " + lineNo + " deve essere tra 0 e 100"
                );
            }

            if (discountPct.stripTrailingZeros().scale() > 2) {
                throw new BusinessException(
                    "Lo sconto della riga " + lineNo +
                    " può avere al massimo 2 decimali"
                );
            }

            if (taxPct.compareTo(BigDecimal.ZERO) < 0 ||
                taxPct.compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException(
                    "L'IVA della riga " + lineNo + " deve essere tra 0 e 100"
                );
            }

            if (taxPct.stripTrailingZeros().scale() > 2) {
                throw new BusinessException(
                    "L'IVA della riga " + lineNo +
                    " può avere al massimo 2 decimali"
                );
            }
        }
    }

    public Long create(Long tenantId, PurchaseOrderForm form) {
        Long companyId = companyContext.resolveCurrentCompanyId();

        validateLines(form.getLines());
        validateHeader(tenantId, companyId, form);

        String orderNumber = nextOrderNumber(tenantId, companyId);

        PurchaseOrder po = new PurchaseOrder();
        po.setTenantId(tenantId);
        po.setCompanyId(companyId);
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
        Long companyId = companyContext.resolveCurrentCompanyId();

        var po = purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        id
                )
                .orElseThrow(() -> new NotFoundException("Ordine fornitore non trovato"));

        if ("CONFIRMED".equalsIgnoreCase(po.getStatus()) || "CANCELLED".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException("Questo ordine fornitore non è più modificabile.");
        }

        validateLines(form.getLines());
        validateHeader(tenantId, po.getCompanyId(), form);
        applyHeader(po, form);

        Totals totals = calculateTotals(form.getLines());
        po.setSubtotalAmount(totals.subtotal());
        po.setTaxAmount(totals.tax());
        po.setTotalAmount(totals.total());

        purchaseOrderRepository.save(po);
        purchaseOrderLineRepository.deleteByTenantIdAndPurchaseOrderId(tenantId, id);
        saveLines(tenantId, id, form.getLines());
    }

    private void validateHeader(Long tenantId, Long companyId, PurchaseOrderForm form) {
        if (form.getSupplierId() == null) {
            throw new BusinessException("Il fornitore è obbligatorio.");
        }

        supplierRepository.findByTenantIdAndId(tenantId, form.getSupplierId())
                .orElseThrow(() -> new BusinessException(
                        "Fornitore non valido per il tenant corrente."
                ));

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
            line.setQuantity(parseOrderQuantity(lineForm.getQuantity()));
            line.setUnitPrice(scale(NumberInputUtils.parseDecimal(lineForm.getUnitPrice(), "unit price"),2));
            line.setDiscountPct(scale(NumberInputUtils.parseDecimal(lineForm.getDiscountPct(),"discount"), 2));
            line.setTaxPct(scale(NumberInputUtils.parseDecimal(lineForm.getTaxPct(), "tax pct"),2));

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

        BigDecimal qty = parseOrderQuantity(line.getQuantity());
        BigDecimal unitPrice = scale(NumberInputUtils.parseDecimal(line.getUnitPrice(), "unit price"),2);
        BigDecimal discountPct = scale(NumberInputUtils.parseDecimal(line.getDiscountPct(),"discount"), 2);
        BigDecimal taxPct = scale(NumberInputUtils.parseDecimal(line.getTaxPct(),"tax pct" ),2);
        BigDecimal gross = qty.multiply(unitPrice);
        BigDecimal discountAmount = gross.multiply(discountPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal subtotal = gross.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(taxPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        return new LineTotals(subtotal, tax, total);
    }

    private BigDecimal parseOrderQuantity(String value) {
        BigDecimal quantity = NumberInputUtils.parseDecimal(value, "quantity");

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità dell'ordine deve essere maggiore di zero."
            );
        }

        if (quantity.stripTrailingZeros().scale() > 3) {
            throw new BusinessException(
                    "La quantità dell'ordine può avere al massimo 3 decimali."
            );
        }

        return quantity.setScale(3, RoundingMode.UNNECESSARY);
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value == null ? BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP)
                : value.setScale(scale, RoundingMode.HALF_UP);
    }

    private PurchaseOrderLineForm defaultLine() {
        PurchaseOrderLineForm line = new PurchaseOrderLineForm();
        line.setQuantity(PdfFormatUtils.formatDecimal(new BigDecimal("1"),0));
        line.setUnitPrice(PdfFormatUtils.formatMoney(BigDecimal.ZERO));
        line.setDiscountPct(PdfFormatUtils.formatDecimal(BigDecimal.ZERO,0));
        line.setTaxPct(PdfFormatUtils.formatDecimal(new BigDecimal("22"),0));
        line.setItemId(null);
        return line;
    }

    private String nextOrderNumber(Long tenantId, Long companyId) {
        return documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.PURCHASE_ORDER
        );
    }

    @Transactional(readOnly = true)
    public PurchaseOrderLineForm buildDefaultLine() {
        PurchaseOrderLineForm line = new PurchaseOrderLineForm();
        line.setItemId(null);
        line.setDescription("");
        line.setQuantity(PdfFormatUtils.formatDecimalTrimmed(java.math.BigDecimal.ONE,0));
        line.setUnitPrice(PdfFormatUtils.formatMoney(java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP)));
        line.setDiscountPct(PdfFormatUtils.formatDecimal(java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP)));
        line.setTaxPct(PdfFormatUtils.formatDecimal(new java.math.BigDecimal("22"),0));
        return line;
    }

    private record LineTotals(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    }

    private record Totals(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    }

    public Page<PurchaseOrderListItemView> findPage(Long tenantId,
                                                    int page,
                                                    int size,
                                                    String q,
                                                    String status,
                                                    LocalDate dateFrom,
                                                    LocalDate dateTo) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("orderDate"), Sort.Order.desc("id"))
        );
        Long companyId = companyContext.resolveCurrentCompanyId();

        Specification<PurchaseOrder> spec = Specification.where(byTenantAndCompany(tenantId, companyId))
                .and(bySearch(q))
                .and(byStatus(status))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));
        return purchaseOrderRepository.findAll(spec, pageable).map(this::toListItemView);
    }

    private Specification<PurchaseOrder> byTenantAndCompany(
            Long tenantId,
            Long companyId
    ) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("tenantId"), tenantId),
                cb.equal(root.get("companyId"), companyId)
        );
    }

    private Specification<PurchaseOrder> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            Join<Object, Object> supplier = root.join("supplier", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), like),
                    cb.like(cb.lower(supplier.get("name")), like)
            );
        };

    }

    private Specification<PurchaseOrder> byStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<PurchaseOrder> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), dateFrom);
    }

    private Specification<PurchaseOrder> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("expectedDeliveryDate"), dateTo);
    }

    private PurchaseOrderListItemView toListItemView(PurchaseOrder order) {

        PurchaseOrderListItemView v = new PurchaseOrderListItemView();
        v.setId(order.getId());
        v.setOrderNumber(order.getOrderNumber());
        v.setFormattedOrderDate(PdfFormatUtils.formatDate(order.getOrderDate()));
        v.setFormattedExpectedDeliveryDate(PdfFormatUtils.formatDate(order.getExpectedDeliveryDate()));
        v.setStatus(order.getStatus());
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(order.getTotalAmount()));
        v.setCurrencyCode(order.getCurrencyCode());
        v.setSupplierName(
                supplierRepository.findByTenantIdAndId(order.getTenantId(), order.getSupplierId())
                        .map(s -> s.getName())
                        .orElse("-")
        );
        return v;

    }
}