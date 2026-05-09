package com.gestiva.accounting.dashboard.web;

import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.entry.repository.AccountingEntryRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountingDashboardWebService {

    private final PaymentDueRepository paymentDueRepository;
    private final AccountingEntryRepository accountingEntryRepository;

    public AccountingDashboardWebService(PaymentDueRepository paymentDueRepository,
                                         AccountingEntryRepository accountingEntryRepository) {
        this.paymentDueRepository = paymentDueRepository;
        this.accountingEntryRepository = accountingEntryRepository;
    }

    public AccountingDashboardView build(Long tenantId) {
        List<String> openStatuses = List.of("OPEN", "PARTIALLY_PAID");

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today;

        BigDecimal openReceivables = nvl(
                paymentDueRepository.sumOpenAmountByDirectionAndStatuses(tenantId, "RECEIVABLE", openStatuses)
        );
        BigDecimal openPayables = nvl(
                paymentDueRepository.sumOpenAmountByDirectionAndStatuses(tenantId, "PAYABLE", openStatuses)
        );

        long overdueCount = paymentDueRepository.countOverdue(tenantId, openStatuses, today);
        BigDecimal overdueAmount = nvl(
                paymentDueRepository.sumOverdueOpenAmount(tenantId, openStatuses, today)
        );

        BigDecimal monthReceipts = nvl(
                accountingEntryRepository.sumTotalByCausalCodeAndPeriod(
                        tenantId, "CUSTOMER_RECEIPT", monthStart, monthEnd
                )
        );

        BigDecimal monthPayments = nvl(
                accountingEntryRepository.sumTotalByCausalCodeAndPeriod(
                        tenantId, "SUPPLIER_PAYMENT", monthStart, monthEnd
                )
        );

        AccountingDashboardView view = new AccountingDashboardView();
        view.setFormattedOpenReceivables(PdfFormatUtils.formatMoney(openReceivables));
        view.setFormattedOpenPayables(PdfFormatUtils.formatMoney(openPayables));
        view.setOverdueCount(overdueCount);
        view.setFormattedOverdueAmount(PdfFormatUtils.formatMoney(overdueAmount));
        view.setFormattedMonthReceipts(PdfFormatUtils.formatMoney(monthReceipts));
        view.setFormattedMonthPayments(PdfFormatUtils.formatMoney(monthPayments));

        accountingEntryRepository.findTop10ByTenantIdOrderByEntryDateDescIdDesc(tenantId)
                .forEach(entry -> {
                    AccountingDashboardRecentEntryView rv = new AccountingDashboardRecentEntryView();
                    rv.setId(entry.getId());
                    rv.setEntryNumber(entry.getEntryNumber());
                    rv.setFormattedEntryDate(PdfFormatUtils.formatDate(entry.getEntryDate()));
                    rv.setCausalCode(entry.getCausalCode());
                    rv.setDescription(entry.getDescription());
                    rv.setFormattedTotalAmount(PdfFormatUtils.formatMoney(entry.getTotalAmount()));
                    view.getRecentEntries().add(rv);
                });

        return view;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}