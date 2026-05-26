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
public class FinancialPositionDetailWebService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;

    public FinancialPositionDetailWebService(JournalEntryRepository journalEntryRepository,
                                             JournalEntryLineRepository journalEntryLineRepository,
                                             AccountRepository accountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.accountRepository = accountRepository;
    }

    public FinancialPositionDetailView build(Long tenantId, LocalDate dateFrom, LocalDate dateTo) {
        List<JournalEntry> entries = journalEntryRepository
                .findByTenantIdAndEntryDateBetweenOrderByEntryDateAscIdAsc(tenantId, dateFrom, dateTo);

        FinancialPositionDetailView view = new FinancialPositionDetailView();
        view.setFormattedDateFrom(PdfFormatUtils.formatDate(dateFrom));
        view.setFormattedDateTo(PdfFormatUtils.formatDate(dateTo));

        if (entries.isEmpty()) {
            return view;
        }

        List<Long> entryIds = entries.stream()
                .map(JournalEntry::getId)
                .toList();

        List<JournalEntryLine> lines = journalEntryLineRepository.findByTenantIdAndJournalEntryIdIn(tenantId, entryIds);

        Map<Long, Account> accountsById = accountRepository.findByTenantIdOrderByCodeAsc(tenantId).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        Map<Long, BigDecimal> balanceByAccountId = new HashMap<>();

        for (JournalEntryLine line : lines) {
            BigDecimal debit = money(line.getDebitAmount());
            BigDecimal credit = money(line.getCreditAmount());
            BigDecimal delta = money(debit.subtract(credit));

            balanceByAccountId.put(
                    line.getAccountId(),
                    money(balanceByAccountId.getOrDefault(line.getAccountId(), zero()).add(delta))
            );
        }

        List<String> orderedTypes = List.of("ASSET", "LIABILITY", "EQUITY", "REVENUE", "COST");
        List<FinancialPositionSectionView> sections = new ArrayList<>();

        for (String type : orderedTypes) {
            List<Account> accountsOfType = accountsById.values().stream()
                    .filter(a -> a.getAccountType() != null)
                    .filter(a -> type.equalsIgnoreCase(a.getAccountType()))
                    .sorted(Comparator.comparing(Account::getCode, Comparator.nullsLast(String::compareTo)))
                    .toList();

            List<FinancialPositionDetailRowView> details = new ArrayList<>();
            BigDecimal sectionTotal = zero();

            for (Account account : accountsOfType) {
                BigDecimal balance = money(balanceByAccountId.getOrDefault(account.getId(), zero()));
                if (balance.compareTo(zero()) == 0) {
                    continue;
                }

                FinancialPositionDetailRowView detail = new FinancialPositionDetailRowView();
                detail.setAccountId(account.getId());
                detail.setAccountCode(account.getCode());
                detail.setAccountName(account.getName());
                detail.setFormattedAmount(PdfFormatUtils.formatMoney(balance));
                details.add(detail);

                sectionTotal = money(sectionTotal.add(balance));
            }

            if (details.isEmpty()) {
                continue;
            }

            FinancialPositionSectionView section = new FinancialPositionSectionView();
            section.setAccountType(type);
            section.setAccountTypeLabel(toLabel(type));
            section.setFormattedSectionTotal(PdfFormatUtils.formatMoney(sectionTotal));
            section.setDetails(details);

            sections.add(section);
        }

        view.setSections(sections);
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