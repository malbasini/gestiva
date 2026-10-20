package com.gestiva.accounting.dashboard.web;

import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.accounting.payment.repository.PaymentTransactionRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;

@Service
@Transactional(readOnly = true)
public class AccountingDashboardWebService {

    private final PaymentDueRepository paymentDueRepository;
    private final CompanyContext companyContext;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final JournalEntryRepository journalEntryRepository;

    public AccountingDashboardWebService(
            PaymentDueRepository paymentDueRepository,
            JournalEntryRepository journalEntryRepository,
            CompanyContext companyContext,
            PaymentTransactionRepository paymentTransactionRepository) {

        this.paymentDueRepository = paymentDueRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.companyContext = companyContext;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    public AccountingDashboardView build(Long tenantId) {

        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new com.gestiva.common.exception.BusinessException(
                    "Nessuna società attiva selezionata."
            );
        }

        List<String> openStatuses = List.of("OPEN", "PARTIALLY_PAID");
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today;

        BigDecimal openReceivables = nvl(
                paymentDueRepository.sumOpenAmountByCompanyAndDirectionAndStatuses(
                        tenantId, companyId, "RECEIVABLE", openStatuses
                )
        );

        BigDecimal openPayables = nvl(
                paymentDueRepository.sumOpenAmountByCompanyAndDirectionAndStatuses(
                        tenantId, companyId, "PAYABLE", openStatuses
                )
        );

        long overdueCount = paymentDueRepository.countOverdueByCompany(
                tenantId, companyId, openStatuses, today
        );

        BigDecimal overdueAmount = nvl(
                paymentDueRepository.sumOverdueOpenAmountByCompany(
                        tenantId, companyId, openStatuses, today
                )
        );

        BigDecimal monthReceipts = nvl(
                paymentTransactionRepository.sumAmountByCompanyAndDirectionAndPeriod(
                        tenantId,
                        companyId,
                        "IN",
                        monthStart,
                        monthEnd
                )
        );

        BigDecimal monthPayments = nvl(
                paymentTransactionRepository.sumAmountByCompanyAndDirectionAndPeriod(
                        tenantId,
                        companyId,
                        "OUT",
                        monthStart,
                        monthEnd
                )
        );

        AccountingDashboardView view = new AccountingDashboardView();
        view.setFormattedOpenReceivables(PdfFormatUtils.formatMoney(openReceivables));
        view.setFormattedOpenPayables(PdfFormatUtils.formatMoney(openPayables));
        view.setOverdueCount(overdueCount);
        view.setFormattedOverdueAmount(PdfFormatUtils.formatMoney(overdueAmount));
        view.setFormattedMonthReceipts(PdfFormatUtils.formatMoney(monthReceipts));
        view.setFormattedMonthPayments(PdfFormatUtils.formatMoney(monthPayments));

        journalEntryRepository.findTop10ByTenantIdAndCompanyIdOrderByEntryDateDescIdDesc(
                        tenantId,
                        companyId
                )
                .forEach(entry -> {
                    AccountingDashboardRecentEntryView rv = new AccountingDashboardRecentEntryView();
                    rv.setId(entry.getId());
                    rv.setEntryNumber(entry.getEntryNumber());
                    rv.setFormattedEntryDate(PdfFormatUtils.formatDate(entry.getEntryDate()));
                    rv.setCausalCode(entry.getCausalCode());
                    rv.setDescription(entry.getDescription());
                    rv.setFormattedTotalAmount(
                            PdfFormatUtils.formatMoney(entry.getTotalDebit())
                    );
                    view.getRecentEntries().add(rv);
                });

        return view;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}