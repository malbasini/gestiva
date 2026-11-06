package com.gestiva.purchasing.receipt.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.purchasing.supplier.entity.Supplier;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GoodsReceiptWebService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final ItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final CompanyContext companyContext;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.core.env.Environment environment;



    public GoodsReceiptWebService(GoodsReceiptRepository goodsReceiptRepository,
                                  GoodsReceiptLineRepository goodsReceiptLineRepository,
                                  ItemRepository itemRepository,
                                  SupplierRepository supplierRepository,
                                  PurchaseOrderRepository purchaseOrderRepository,
                                  PurchaseOrderLineRepository purchaseOrderLineRepository,
                                  SupplierInvoiceRepository supplierInvoiceRepository,
                                  CompanyContext companyContext) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.itemRepository = itemRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.companyContext = companyContext;
    }

    public List<GoodsReceiptListItemView> findAll(Long tenantId) {
        Long companyId = companyContext.requireCurrentCompanyId();

        var receipts = goodsReceiptRepository.findAll(
                org.springframework.data.jpa.domain.Specification
                        .where(byTenantAndCompany(tenantId, companyId)),
                Sort.by(Sort.Direction.DESC, "receiptDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        List<GoodsReceiptListItemView> result = new ArrayList<>();
        for (var receipt : receipts) {
            GoodsReceiptListItemView v = new GoodsReceiptListItemView();
            v.setId(receipt.getId());
            v.setReceiptNumber(receipt.getReceiptNumber());
            v.setFormattedReceiptDate(PdfFormatUtils.formatDate(receipt.getReceiptDate()));
            v.setSupplierName(
                    supplierRepository.findByTenantIdAndId(tenantId, receipt.getSupplierId())
                            .map(s -> s.getName())
                            .orElse("-")
            );
            v.setPurchaseOrderNumber(
                    purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(
                            tenantId,
                            receipt.getCompanyId(),
                            receipt.getPurchaseOrderId()
                    )
                            .map(po -> po.getOrderNumber())
                            .orElse("-")
            );
            result.add(v);
        }
        return result;
    }

    public GoodsReceiptDetailView getDetail(Long tenantId, Long id) {
        Long companyId = companyContext.requireCurrentCompanyId();

        var receipt = goodsReceiptRepository.findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        id
                )
                .orElseThrow(() -> new NotFoundException("Ricezione merci non trovata"));

        var lines = goodsReceiptLineRepository.findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(tenantId, companyId, id);

        GoodsReceiptDetailView v = new GoodsReceiptDetailView();
        v.setId(receipt.getId());
        v.setReceiptNumber(receipt.getReceiptNumber());
        v.setFormattedReceiptDate(PdfFormatUtils.formatDate(receipt.getReceiptDate()));
        v.setSupplierName(
                supplierRepository.findByTenantIdAndId(tenantId, receipt.getSupplierId())
                        .map(s -> s.getName())
                        .orElse("-")
        );
        v.setPurchaseOrderNumber(
                purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(
                            tenantId,
                            receipt.getCompanyId(),
                            receipt.getPurchaseOrderId()
                    )
                        .map(po -> po.getOrderNumber())
                        .orElse("-")
        );
        v.setNotes(receipt.getNotes());
        v.setPurchaseOrderId(receipt.getPurchaseOrderId());

        var invoiceOpt = supplierInvoiceRepository
                .findFirstByTenantIdAndCompanyIdAndGoodsReceiptId(
                        tenantId,
                        receipt.getCompanyId(),
                        receipt.getId()
                );
        v.setHasSupplierInvoice(invoiceOpt.isPresent());

        // Classificazione per la sola presentazione UI.
        boolean testProfileActive = environment != null
                && java.util.Arrays.asList(
                        environment.getActiveProfiles()
                ).contains("test");

        boolean generalEnabled = environment != null
                && environment.getProperty(
                        "gestiva.purchasing.supplier-invoice.enabled",
                        Boolean.class,
                        false
                );

        boolean partialEnabled = environment != null
                && environment.getProperty(
                        "gestiva.purchasing.supplier-invoice.partial.enabled",
                        Boolean.class,
                        false
                );

        var authentication =
                org.springframework.security.core.context
                        .SecurityContextHolder.getContext()
                        .getAuthentication();

        boolean authorizedRole = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(authority.getAuthority())
                                || "ROLE_PURCHASING".equals(
                                        authority.getAuthority()
                                )
                        );

        var classification =
                GoodsReceiptInvoiceQuantityClassifier.Result.INVALID;

        boolean confirmedOrder = false;

        if (testProfileActive
                && generalEnabled
                && authorizedRole
                && invoiceOpt.isEmpty()) {

            var orderOpt = purchaseOrderRepository
                    .findByTenantIdAndCompanyIdAndId(
                            tenantId,
                            companyId,
                            receipt.getPurchaseOrderId()
                    );

            if (orderOpt.isPresent()) {
                var order = orderOpt.get();

                confirmedOrder =
                        "CONFIRMED".equalsIgnoreCase(order.getStatus())
                        && java.util.Objects.equals(
                                order.getSupplierId(),
                                receipt.getSupplierId()
                        );

                if (confirmedOrder) {
                    var orderLines = purchaseOrderLineRepository
                            .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                                    tenantId,
                                    order.getId()
                            );

                    classification =
                            GoodsReceiptInvoiceQuantityClassifier.classify(
                                    orderLines,
                                    lines
                            );
                }
            }
        }

        var actions = GoodsReceiptInvoiceActionPolicy.evaluate(
                classification,
                confirmedOrder,
                invoiceOpt.isPresent(),
                authorizedRole,
                testProfileActive,
                generalEnabled,
                partialEnabled
        );

        v.setCanCreateTraditionalSupplierInvoice(
                actions.traditional()
        );

        v.setCanCreatePartialSupplierInvoice(
                actions.partial()
        );

        // Il template legacy continua a leggere questo campo.
        v.setCanCreateSupplierInvoice(
                actions.traditional()
        );

        invoiceOpt.ifPresent(invoice -> {
            v.setSupplierInvoiceId(invoice.getId());
            v.setSupplierInvoiceNumber(invoice.getInvoiceNumber());
        });

        for (var line : lines) {
            GoodsReceiptDetailLineView lv = new GoodsReceiptDetailLineView();
            lv.setLineNo(line.getLineNo());
            lv.setDescription(line.getDescription());
            lv.setFormattedQuantityReceived(PdfFormatUtils.formatDecimalTrimmed(line.getQuantityReceived(), 3));
            lv.setUnitOfMeasureCode(
                    line.getItemId() == null
                            ? "-"
                            : itemRepository
                                    .findWithBaseUomByTenantIdAndId(tenantId, line.getItemId())
                                    .map(item -> item.getBaseUom())
                                    .map(uom -> uom.getCode())
                                    .orElse("-")
            );
            v.getLines().add(lv);
        }

        return v;
    }
    public Page<GoodsReceiptListItemView> findPage(Long tenantId,
                                                      int page,
                                                      int size,
                                                      String q,
                                                      LocalDate dateFrom,
                                                      LocalDate dateTo) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("receiptDate"), Sort.Order.desc("id"))
        );
        Long companyId = companyContext.requireCurrentCompanyId();

        Specification<GoodsReceipt> spec = Specification.where(
                        byTenantAndCompany(tenantId, companyId))
                .and(bySearch(q))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));
        return goodsReceiptRepository.findAll(spec, pageable).map(this::toListItemView);

    }

    private Specification<GoodsReceipt> byTenantAndCompany(
            Long tenantId,
            Long companyId
    ) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("tenantId"), tenantId),
                cb.equal(root.get("companyId"), companyId)
        );
    }

    private Specification<GoodsReceipt> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("receiptNumber")), like);
    }

    private Specification<GoodsReceipt> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("receiptDate"), dateFrom);
    }

    private Specification<GoodsReceipt> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("receiptDate"), dateTo);
    }

    private GoodsReceiptListItemView toListItemView(GoodsReceipt receipt) {

        GoodsReceiptListItemView v = new GoodsReceiptListItemView();
        v.setId(receipt.getId());
        v.setReceiptNumber(receipt.getReceiptNumber());
        v.setSupplierName(
                supplierRepository.findByTenantIdAndId(receipt.getTenantId(), receipt.getSupplierId())
                        .map(Supplier::getName)
                        .orElse("-")
        );
        v.setFormattedReceiptDate(PdfFormatUtils.formatDate(receipt.getReceiptDate()));
        v.setPurchaseOrderId(receipt.getPurchaseOrderId());
        v.setPurchaseOrderNumber(
                purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(
                        receipt.getTenantId(),
                        receipt.getCompanyId(),
                        receipt.getPurchaseOrderId()
                )
                        .map(PurchaseOrder::getOrderNumber)
                        .orElse("-")
        );
        return v;

    }
}