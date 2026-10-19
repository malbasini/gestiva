package com.gestiva.sales.order.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import com.gestiva.sales.order.repository.SalesOrderLineRepository;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.warehouse.item.entity.ItemType;
import com.gestiva.warehouse.item.repository.ItemRepository;

@Service
@Transactional(readOnly = true)
public class SalesOrderDetailWebService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final CustomerRepository customerRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final ItemRepository itemRepository;
    private final CompanyContext companyContext;



    public SalesOrderDetailWebService(
            SalesOrderRepository salesOrderRepository,
            SalesOrderLineRepository salesOrderLineRepository,
            CustomerRepository customerRepository,
            DeliveryNoteRepository deliveryNoteRepository,
            InvoiceRepository invoiceRepository,
            ItemRepository itemRepository,
            CompanyContext companyContext) {

        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.customerRepository = customerRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.itemRepository = itemRepository;
        this.companyContext = companyContext;
    }

    private Long requireCompanyId() {

        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata.");
        }
        return companyId;
    }

    public SalesOrderDetailView getDetail(Long tenantId, Long orderId) {
        Long companyId = requireCompanyId();

        var order = salesOrderRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        orderId)
                .orElseThrow(() -> new NotFoundException(
                        "Ordine non trovato nella società attiva"));

        var customer = customerRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        order.getCustomerId()
                )
                .orElseThrow(() -> new NotFoundException(
                        "Cliente non trovato nella società attiva"
                ));

        var lines = salesOrderLineRepository
                .findByTenantIdAndCompanyIdAndSalesOrderIdOrderByLineNoAsc(
                        tenantId,
                        companyId,
                        orderId);

        SalesOrderDetailView view = new SalesOrderDetailView();
        view.setId(order.getId());
        view.setCustomerId(customer.getId());
        view.setCustomerName(customer.getName());
        view.setQuoteId(order.getQuoteId());
        view.setOrderNumber(order.getOrderNumber());
        view.setStatus(order.getStatus());
        view.setCurrencyCode(order.getCurrencyCode());

        view.setFormattedOrderDate(PdfFormatUtils.formatDate(order.getOrderDate()));
        view.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(order.getSubtotalAmount()));
        view.setFormattedTaxAmount(PdfFormatUtils.formatMoney(order.getTaxAmount()));
        view.setFormattedTotalAmount(PdfFormatUtils.formatMoney(order.getTotalAmount()));

        view.setLines(lines.stream().map(line -> {
            SalesOrderDetailLineView l = new SalesOrderDetailLineView();
            l.setLineNo(line.getLineNo());
            l.setDescription(line.getDescription());
            l.setFormattedQuantity(PdfFormatUtils.formatDecimalTrimmed(line.getQuantity(),2));
            l.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            l.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()));
            l.setFormattedTaxPct(PdfFormatUtils.formatDecimalTrimmed(line.getTaxPct(),2));
            java.math.BigDecimal gross = line.getQuantity().multiply(line.getUnitPrice());
            java.math.BigDecimal discountAmount = gross
                    .multiply(line.getDiscountPct())
                    .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal taxable = gross.subtract(discountAmount);
            l.setFormattedLineTotal(PdfFormatUtils.formatMoney(taxable));
            return l;
        }).toList());
        view.setActionable("CONFIRMED".equals(order.getStatus()));
        var deliveryNoteOpt = deliveryNoteRepository
                .findByTenantIdAndCompanyIdAndSalesOrderId(
                        tenantId,
                        companyId,
                        orderId
                );
        boolean deliveryNoteExists = deliveryNoteOpt.isPresent();
        view.setDeliveryNoteExists(deliveryNoteExists);
        // Verifica se tutte le righe sono servizi.
        boolean serviceOnly = !lines.isEmpty()
                && lines.stream().allMatch(line -> {

            if (line.getItemId() == null) {
                return false;
            }

            return itemRepository
                    .findByTenantIdAndId(
                            tenantId,
                            line.getItemId()
                    )
                    .map(item ->
                            item.getItemType() == ItemType.SERVICE
                    )
                    .orElse(false);
        });

// Recupera le fatture già associate all'ordine.
        var invoices = invoiceRepository
                .findByTenantIdAndCompanyIdAndSalesOrderIdOrderByInvoiceDateDescIdDesc(
                        tenantId,
                        companyId,
                        orderId
                );

        boolean alreadyInvoiced = !invoices.isEmpty();

        // Espone il collegamento alla fattura esistente.
        if (alreadyInvoiced) {
            var invoice = invoices.get(0);

            view.setInvoiceId(invoice.getId());
            view.setInvoiceNumber(invoice.getInvoiceNumber());
        }

       // Fatturazione diretta: esclusivamente servizi.
        view.setDirectInvoiceCreatable(
                "FULFILLED".equals(order.getStatus())
                        && serviceOnly
                        && !deliveryNoteExists
                        && !alreadyInvoiced
        );

       // Creazione DDT: prodotti o ordini misti.
        view.setDeliveryNoteCreatable(
                "FULFILLED".equals(order.getStatus())
                        && !serviceOnly
                        && !deliveryNoteExists
                        && !alreadyInvoiced
        );
        deliveryNoteOpt.ifPresent(ddt -> {
            view.setDeliveryNoteId(ddt.getId());
            view.setDeliveryNoteNumber(ddt.getDdtNumber());
        });
        return view;
    }
}