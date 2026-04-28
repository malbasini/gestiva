package com.gestiva.sales.order.pdf;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.platform.company.entity.CompanyProfile;
import com.gestiva.platform.company.repository.CompanyProfileRepository;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.platform.tenant.repository.TenantRepository;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.entity.SalesOrderLine;
import com.gestiva.sales.order.repository.SalesOrderLineRepository;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SalesOrderPdfDataService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final CustomerRepository customerRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final TenantRepository tenantRepository;

    public SalesOrderPdfDataService(SalesOrderRepository salesOrderRepository,
                                    SalesOrderLineRepository salesOrderLineRepository,
                                    CustomerRepository customerRepository,
                                    CompanyProfileRepository companyProfileRepository,
                                    TenantRepository tenantRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.customerRepository = customerRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.tenantRepository = tenantRepository;
    }

    public SalesOrderPdfView buildView(Long tenantId, Long orderId) {
        SalesOrder order = salesOrderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new NotFoundException("Ordine non trovato"));

        List<SalesOrderLine> lines =
                salesOrderLineRepository.findByTenantIdAndSalesOrderIdOrderByLineNoAsc(tenantId, orderId);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, order.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        CompanyProfile company = companyProfileRepository.findByTenantId(tenantId).orElse(null);
        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);

        SalesOrderPdfView view = new SalesOrderPdfView();

        if (company != null) {
            view.setCompanyName(company.getLegalName());
            view.setCompanyEmail(company.getEmail());
            view.setCompanyPhone(company.getPhone());
            view.setCompanyVatNumber(company.getVatNumber());
        } else if (tenant != null) {
            view.setCompanyName(tenant.getName());
            view.setCompanyEmail(tenant.getEmail());
            view.setCompanyPhone("");
            view.setCompanyVatNumber("");
        } else {
            view.setCompanyName("Azienda Demo");
            view.setCompanyEmail("");
            view.setCompanyPhone("");
            view.setCompanyVatNumber("");
        }
        view.setCustomerName(customer.getName());
        view.setCustomerEmail(customer.getEmail());
        view.setCustomerVatNumber(customer.getVatNumber());
        view.setOrderId(order.getId());
        view.setQuoteId(order.getQuoteId());
        view.setOrderNumber(order.getOrderNumber());
        view.setOrderDate(order.getOrderDate());
        view.setCurrencyCode(order.getCurrencyCode());
        view.setStatus(order.getStatus());
        view.setSubtotalAmount(order.getSubtotalAmount());
        view.setTaxAmount(order.getTaxAmount());
        view.setTotalAmount(order.getTotalAmount());
        view.setFormattedOrderDate(PdfFormatUtils.formatDate(order.getOrderDate()));
        view.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(order.getSubtotalAmount()));
        view.setFormattedTaxAmount(PdfFormatUtils.formatMoney(order.getTaxAmount()));
        view.setFormattedTotalAmount(PdfFormatUtils.formatMoney(order.getTotalAmount()));
        view.setLines(lines.stream().map(line -> {
            SalesOrderPdfLineView l = new SalesOrderPdfLineView();
            l.setLineNo(line.getLineNo());
            l.setDescription(line.getDescription());
            l.setQuantity(line.getQuantity());
            l.setUnitPrice(line.getUnitPrice());
            l.setDiscountPct(line.getDiscountPct());
            l.setTaxPct(line.getTaxPct());
            l.setTaxAmount(line.getTaxAmount());
            l.setLineTotal(line.getLineTotal());
            l.setFormattedQuantity(PdfFormatUtils.formatDecimal(line.getQuantity()));
            l.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            l.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()));
            l.setFormattedTaxPct(PdfFormatUtils.formatDecimal(line.getTaxPct()));
            l.setFormattedTaxAmount(PdfFormatUtils.formatMoney(line.getTaxAmount()));
            l.setFormattedLineTotal(PdfFormatUtils.formatMoney(line.getLineTotal()));
            return l;

        }).toList());

        return view;
    }
}