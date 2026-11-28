package com.gestiva.accounting.v2.report.web;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.security.company.context.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FinancialPositionSummaryWebService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;
    private final CompanyContext companyContext;








    public FinancialPositionSummaryWebService(JournalEntryRepository journalEntryRepository,
                                              JournalEntryLineRepository journalEntryLineRepository,
                                              AccountRepository accountRepository,
                                              CompanyContext companyContext) {

        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.accountRepository = accountRepository;
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





    public FinancialPositionSummaryView build(Long tenantId, LocalDate dateFrom, LocalDate dateTo) {
        List<JournalEntry> entries = journalEntryRepository
                .findByTenantIdAndCompanyIdAndEntryDateBetweenOrderByEntryDateAscIdAsc(tenantId,requireCompanyId(), dateFrom, dateTo);

        FinancialPositionSummaryView view = new FinancialPositionSummaryView();
        view.setFormattedDateFrom(PdfFormatUtils.formatDate(dateFrom));
        view.setFormattedDateTo(PdfFormatUtils.formatDate(dateTo));

        if (entries.isEmpty()) {
            return view;
        }

        List<Long> entryIds = entries.stream()
                .map(JournalEntry::getId)
                .toList();

        List<JournalEntryLine> lines = journalEntryLineRepository.findByTenantIdAndCompanyIdAndJournalEntryIdIn(tenantId,requireCompanyId(), entryIds);

        Map<Long, Account> accountsById = accountRepository.findByTenantIdAndCompanyIdOrderByCodeAsc(tenantId,requireCompanyId()).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        Map<String, BigDecimal> totalsByType = new LinkedHashMap<>();
        totalsByType.put("ASSET", zero());
        totalsByType.put("LIABILITY", zero());
        totalsByType.put("EQUITY", zero());
        totalsByType.put("REVENUE", zero());
        totalsByType.put("COST", zero());

        for (JournalEntryLine line : lines) {
            Account account = accountsById.get(line.getAccountId());
            if (account == null || account.getAccountType() == null) {
                continue;
            }

            BigDecimal debit = money(line.getDebitAmount());
            BigDecimal credit = money(line.getCreditAmount());
            BigDecimal balance = money(debit.subtract(credit));

            String type = account.getAccountType().trim().toUpperCase(Locale.ROOT);
            totalsByType.put(type, money(totalsByType.getOrDefault(type, zero()).add(balance)));
        }

        List<FinancialPositionSummaryRowView> rows = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : totalsByType.entrySet()) {
            if (entry.getValue().compareTo(zero()) == 0) {
                continue;
            }

            FinancialPositionSummaryRowView row = new FinancialPositionSummaryRowView();
            row.setAccountType(entry.getKey());
            row.setAccountTypeLabel(toLabel(entry.getKey()));
            row.setFormattedAmount(PdfFormatUtils.formatMoney(entry.getValue()));
            rows.add(row);
        }

        view.setRows(rows);
        return view;
    }

    private String toLabel(String accountType) {
        return switch (accountType) {
            case "ASSET" -> "Attività";
            case "LIABILITY" -> "Passività";
            case "EQUITY" -> "Patrimonio netto";
            case "REVENUE" -> "Ricavi";
            case "COST" -> "Costi";
            default -> accountType;
        };
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? zero() : value.setScale(2, RoundingMode.HALF_UP);
    }
}