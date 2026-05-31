package com.gestiva.accounting.v2.report.web;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class IncomeStatementWebService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;

    public IncomeStatementWebService(JournalEntryRepository journalEntryRepository,
                                     JournalEntryLineRepository journalEntryLineRepository,
                                     AccountRepository accountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.accountRepository = accountRepository;
    }

    public IncomeStatementView build(Long tenantId, LocalDate dateFrom, LocalDate dateTo) {
        List<JournalEntry> entries = journalEntryRepository
                .findByTenantIdAndEntryDateBetweenOrderByEntryDateAscIdAsc(tenantId, dateFrom, dateTo);

        IncomeStatementView view = new IncomeStatementView();
        view.setFormattedDateFrom(PdfFormatUtils.formatDate(dateFrom));
        view.setFormattedDateTo(PdfFormatUtils.formatDate(dateTo));

        if (entries.isEmpty()) {
            view.setFormattedTotalRevenue(PdfFormatUtils.formatMoney(zero()));
            view.setFormattedTotalCost(PdfFormatUtils.formatMoney(zero()));
            view.setFormattedPeriodResult(PdfFormatUtils.formatMoney(zero()));
            return view;
        }

        List<Long> entryIds = entries.stream().map(JournalEntry::getId).toList();
        List<JournalEntryLine> lines = journalEntryLineRepository.findByTenantIdAndJournalEntryIdIn(tenantId, entryIds);

        Map<Long, Account> accountsById = accountRepository.findByTenantIdOrderByCodeAsc(tenantId).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        Map<Long, BigDecimal> balancesByAccountId = new HashMap<>();

        for (JournalEntryLine line : lines) {
            BigDecimal debit = money(line.getDebitAmount());
            BigDecimal credit = money(line.getCreditAmount());
            BigDecimal delta = money(debit.subtract(credit));

            balancesByAccountId.put(
                    line.getAccountId(),
                    money(balancesByAccountId.getOrDefault(line.getAccountId(), zero()).add(delta))
            );
        }

        IncomeStatementSectionView revenueSection = new IncomeStatementSectionView();
        revenueSection.setSectionCode("REVENUE");
        revenueSection.setSectionLabel("Ricavi");

        IncomeStatementSectionView costSection = new IncomeStatementSectionView();
        costSection.setSectionCode("COST");
        costSection.setSectionLabel("Costi");

        BigDecimal totalRevenue = zero();
        BigDecimal totalCost = zero();

        List<Account> orderedAccounts = accountRepository.findByTenantIdOrderByCodeAsc(tenantId);

        for (Account account : orderedAccounts) {
            BigDecimal balance = money(balancesByAccountId.getOrDefault(account.getId(), zero()));
            if (balance.compareTo(zero()) == 0) {
                continue;
            }

            if ("REVENUE".equalsIgnoreCase(account.getAccountType())) {
                BigDecimal amount = money(balance.negate()); // ricavi da mostrare positivi
                if (amount.compareTo(zero()) == 0) continue;

                IncomeStatementRowView row = new IncomeStatementRowView();
                row.setAccountId(account.getId());
                row.setAccountCode(account.getCode());
                row.setAccountName(account.getName());
                row.setFormattedAmount(PdfFormatUtils.formatMoney(amount));
                revenueSection.getRows().add(row);

                totalRevenue = money(totalRevenue.add(amount));
            }

            if ("COST".equalsIgnoreCase(account.getAccountType())) {
                BigDecimal amount = balance; // costi già positivi con Dare - Avere
                if (amount.compareTo(zero()) == 0) continue;

                IncomeStatementRowView row = new IncomeStatementRowView();
                row.setAccountId(account.getId());
                row.setAccountCode(account.getCode());
                row.setAccountName(account.getName());
                row.setFormattedAmount(PdfFormatUtils.formatMoney(amount));
                costSection.getRows().add(row);

                totalCost = money(totalCost.add(amount));
            }
        }

        revenueSection.setFormattedTotal(PdfFormatUtils.formatMoney(totalRevenue));
        costSection.setFormattedTotal(PdfFormatUtils.formatMoney(totalCost));

        if (!revenueSection.getRows().isEmpty()) {
            view.getSections().add(revenueSection);
        }
        if (!costSection.getRows().isEmpty()) {
            view.getSections().add(costSection);
        }

        BigDecimal periodResult = money(totalRevenue.subtract(totalCost));

        view.setFormattedTotalRevenue(PdfFormatUtils.formatMoney(totalRevenue));
        view.setFormattedTotalCost(PdfFormatUtils.formatMoney(totalCost));
        view.setFormattedPeriodResult(PdfFormatUtils.formatMoney(periodResult));

        return view;
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? zero() : value.setScale(2, RoundingMode.HALF_UP);
    }
}