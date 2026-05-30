package com.gestiva.accounting.v2.report.web;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AccountLedgerWebService {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    public AccountLedgerWebService(AccountRepository accountRepository,
                                   JournalEntryRepository journalEntryRepository,
                                   JournalEntryLineRepository journalEntryLineRepository) {
        this.accountRepository = accountRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
    }

    public AccountLedgerView build(Long tenantId, Long accountId, LocalDate dateFrom, LocalDate dateTo) {
        Account account = accountRepository.findByTenantIdAndId(tenantId, accountId)
                .orElseThrow(() -> new BusinessException("Conto non trovato."));

        List<JournalEntry> entries = journalEntryRepository
                .findByTenantIdAndEntryDateBetweenOrderByEntryDateAscIdAsc(tenantId, dateFrom, dateTo);

        Map<Long, JournalEntry> entryById = new HashMap<>();
        for (JournalEntry entry : entries) {
            entryById.put(entry.getId(), entry);
        }

        List<Long> entryIds = entries.stream().map(JournalEntry::getId).toList();

        List<JournalEntryLine> lines = entryIds.isEmpty()
                ? List.of()
                : journalEntryLineRepository.findByTenantIdAndAccountIdAndJournalEntryIdInOrderByJournalEntryIdAscLineNoAsc(
                tenantId, accountId, entryIds
        );

        lines = lines.stream()
                .sorted(Comparator
                        .comparing((JournalEntryLine l) -> entryById.get(l.getJournalEntryId()).getEntryDate())
                        .thenComparing(JournalEntryLine::getJournalEntryId)
                        .thenComparing(JournalEntryLine::getLineNo))
                .toList();

        AccountLedgerView view = new AccountLedgerView();
        view.setAccountId(account.getId());
        view.setAccountCode(account.getCode());
        view.setAccountName(account.getName());
        view.setAccountTypeLabel(toAccountTypeLabel(account.getAccountType()));
        view.setNatureLabel(toNatureLabel(account.getNature()));
        view.setFormattedDateFrom(PdfFormatUtils.formatDate(dateFrom));
        view.setFormattedDateTo(PdfFormatUtils.formatDate(dateTo));

        BigDecimal progressiveBalance = zero();

        for (JournalEntryLine line : lines) {
            JournalEntry entry = entryById.get(line.getJournalEntryId());
            if (entry == null) {
                continue;
            }

            BigDecimal debit = money(line.getDebitAmount());
            BigDecimal credit = money(line.getCreditAmount());
            progressiveBalance = money(progressiveBalance.add(debit).subtract(credit));

            AccountLedgerRowView row = new AccountLedgerRowView();
            row.setFormattedEntryDate(PdfFormatUtils.formatDate(entry.getEntryDate()));
            row.setEntryNumber(entry.getEntryNumber());
            row.setCausalCodeLabel(toCausalLabel(entry.getCausalCode()));
            row.setDescription(line.getDescription());
            row.setFormattedDebit(PdfFormatUtils.formatMoney(debit));
            row.setFormattedCredit(PdfFormatUtils.formatMoney(credit));
            row.setFormattedProgressiveBalance(PdfFormatUtils.formatMoney(progressiveBalance));
            row.setJournalEntryId(entry.getId());

            view.getRows().add(row);
        }

        view.setFormattedFinalBalance(PdfFormatUtils.formatMoney(progressiveBalance));
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

    private String toCausalLabel(String code) {
        if (code == null) return "";
        return switch (code) {
            case "MANUAL_JOURNAL" -> "Scrittura manuale";
            case "CUSTOMER_RECEIPT" -> "Incasso cliente";
            case "SUPPLIER_PAYMENT" -> "Pagamento fornitore";
            case "SALES_INVOICE" -> "Fattura cliente";
            case "PURCHASE_INVOICE" -> "Fattura fornitore";
            default -> code;
        };
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? zero() : value.setScale(2, RoundingMode.HALF_UP);
    }
}
