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
public class TrialBalanceWebService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;

    public TrialBalanceWebService(JournalEntryRepository journalEntryRepository,
                                  JournalEntryLineRepository journalEntryLineRepository,
                                  AccountRepository accountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.accountRepository = accountRepository;
    }

    public TrialBalanceView build(Long tenantId, LocalDate dateFrom, LocalDate dateTo) {
        List<JournalEntry> entries = journalEntryRepository
                .findByTenantIdAndEntryDateBetweenOrderByEntryDateAscIdAsc(tenantId, dateFrom, dateTo);

        TrialBalanceView view = new TrialBalanceView();
        view.setFormattedDateFrom(PdfFormatUtils.formatDate(dateFrom));
        view.setFormattedDateTo(PdfFormatUtils.formatDate(dateTo));

        if (entries.isEmpty()) {
            view.setFormattedGrandTotalDebit(PdfFormatUtils.formatMoney(zero()));
            view.setFormattedGrandTotalCredit(PdfFormatUtils.formatMoney(zero()));
            return view;
        }

        List<Long> entryIds = entries.stream()
                .map(JournalEntry::getId)
                .toList();

        List<JournalEntryLine> lines = journalEntryLineRepository.findByTenantIdAndJournalEntryIdIn(tenantId, entryIds);

        Map<Long, Account> accountsById = accountRepository.findByTenantIdOrderByCodeAsc(tenantId).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        Map<Long, BalanceBucket> bucketsByAccountId = new TreeMap<>();

        BigDecimal grandTotalDebit = zero();
        BigDecimal grandTotalCredit = zero();

        for (JournalEntryLine line : lines) {
            BalanceBucket bucket = bucketsByAccountId.computeIfAbsent(line.getAccountId(), k -> new BalanceBucket());

            BigDecimal debit = money(line.getDebitAmount());
            BigDecimal credit = money(line.getCreditAmount());

            bucket.totalDebit = money(bucket.totalDebit.add(debit));
            bucket.totalCredit = money(bucket.totalCredit.add(credit));

            grandTotalDebit = money(grandTotalDebit.add(debit));
            grandTotalCredit = money(grandTotalCredit.add(credit));
        }

        List<TrialBalanceRowView> rows = new ArrayList<>();

        for (Map.Entry<Long, BalanceBucket> entry : bucketsByAccountId.entrySet()) {
            Long accountId = entry.getKey();
            BalanceBucket bucket = entry.getValue();

            Account account = accountsById.get(accountId);
            if (account == null) {
                continue;
            }

            BigDecimal balance = money(bucket.totalDebit.subtract(bucket.totalCredit));

            TrialBalanceRowView row = new TrialBalanceRowView();
            row.setAccountId(account.getId());
            row.setAccountCode(account.getCode());
            row.setAccountName(account.getName());
            row.setAccountType(this.toAccountTypeLabel(account.getAccountType()));
            row.setNature(this.toNatureLabel(account.getNature()));
            row.setFormattedTotalDebit(PdfFormatUtils.formatMoney(bucket.totalDebit));
            row.setFormattedTotalCredit(PdfFormatUtils.formatMoney(bucket.totalCredit));
            row.setFormattedBalance(PdfFormatUtils.formatMoney(balance));

            rows.add(row);
        }

        rows.sort(Comparator.comparing(TrialBalanceRowView::getAccountCode, Comparator.nullsLast(String::compareTo)));

        view.setRows(rows);
        view.setFormattedGrandTotalDebit(PdfFormatUtils.formatMoney(grandTotalDebit));
        view.setFormattedGrandTotalCredit(PdfFormatUtils.formatMoney(grandTotalCredit));

        return view;
    }

    private String toAccountTypeLabel(String value) {
        if (value == null) return "";
        return switch (value.trim().toUpperCase()) {
            case "ASSET" -> "Attività";
            case "LIABILITY" -> "Passività";
            case "EQUITY" -> "Patrimonio netto";
            case "REVENUE" -> "Ricavi";
            case "COST" -> "Costi";
            default -> value;
        };
    }

    private String toNatureLabel(String value) {
        if (value == null) return "";
        return switch (value.trim().toUpperCase()) {
            case "DEBIT" -> "Dare";
            case "CREDIT" -> "Avere";
            default -> value;
        };
    }
    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? zero() : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static class BalanceBucket {
        private BigDecimal totalDebit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal totalCredit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}