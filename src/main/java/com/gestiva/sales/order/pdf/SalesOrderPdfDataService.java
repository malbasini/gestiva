package com.gestiva.sales.order.pdf;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.platform.company.entity.CompanyProfile;
import com.gestiva.platform.company.repository.CompanyProfileRepository;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.entity.SalesOrderLine;
import com.gestiva.sales.order.repository.SalesOrderLineRepository;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import java.util.List;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;



@Service
@Transactional(readOnly = true)
public class SalesOrderPdfDataService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final CustomerRepository customerRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final CompanyContext companyContext;
    private final CompanyRepository companyRepository;

    public SalesOrderPdfDataService(
            SalesOrderRepository salesOrderRepository,
            SalesOrderLineRepository salesOrderLineRepository,
            CustomerRepository customerRepository,
            CompanyProfileRepository companyProfileRepository,
            CompanyRepository companyRepository,
            CompanyContext companyContext) {

        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.customerRepository = customerRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    private Long requireCompanyId() {

        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata."
            );
        }
        return companyId;
    }

    public SalesOrderPdfView buildView(Long tenantId, Long orderId) {
        Long companyId = requireCompanyId();
        Company issuingCompany = companyRepository
                .findByTenantIdAndId(tenantId, companyId)
                .orElseThrow(() -> new NotFoundException(
                        "Società emittente non trovata"
                ));

        if (!issuingCompany.isActive()) {
            throw new BusinessException(
                    "La società emittente non è attiva."
            );
        }
        SalesOrder order = salesOrderRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        orderId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Ordine non trovato nella società attiva"
                ));

        List<SalesOrderLine> lines =
                salesOrderLineRepository
                        .findByTenantIdAndCompanyIdAndSalesOrderIdOrderByLineNoAsc(
                                tenantId,
                                companyId,
                                orderId
                        );
        Customer customer = customerRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        order.getCustomerId()
                )
                .orElseThrow(() -> new NotFoundException(
                        "Cliente non trovato nella società attiva"
                ));
        CompanyProfile company = companyProfileRepository
                .findByTenantIdAndCompanyId(
                        tenantId,
                        companyId
                )
                .orElse(null);

        SalesOrderPdfView view = new SalesOrderPdfView();
        view.setCompanyName(
                firstNonBlank(
                        company != null ? company.getLegalName() : null,
                        issuingCompany.getLegalName()
                )
        );

        view.setCompanyEmail(
                firstNonBlank(
                        company != null ? company.getEmail() : null,
                        issuingCompany.getEmail()
                )
        );

        view.setCompanyPhone(
                firstNonBlank(
                        company != null ? company.getPhone() : null,
                        issuingCompany.getPhone()
                )
        );

        view.setCompanyVatNumber(
                firstNonBlank(
                        company != null ? company.getVatNumber() : null,
                        issuingCompany.getVatNumber()
                )
        );
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
            l.setFormattedQuantity(PdfFormatUtils.formatDecimalTrimmed(line.getQuantity(),2));
            l.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            l.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()));
            l.setFormattedTaxPct(PdfFormatUtils.formatDecimalTrimmed(line.getTaxPct(),2));
            l.setFormattedTaxAmount(PdfFormatUtils.formatMoney(line.getTaxAmount()));
            java.math.BigDecimal gross = line.getQuantity().multiply(line.getUnitPrice());
            java.math.BigDecimal discountAmount = gross
                    .multiply(line.getDiscountPct())
                    .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal taxable = gross.subtract(discountAmount);
            l.setFormattedLineTotal(PdfFormatUtils.formatMoney(taxable));
            return l;

        }).toList());

        return view;
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "";
    }
}