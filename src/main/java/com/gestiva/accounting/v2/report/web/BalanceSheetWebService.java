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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class BalanceSheetWebService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;

    public BalanceSheetWebService(JournalEntryRepository journalEntryRepository,
                                  JournalEntryLineRepository journalEntryLineRepository,
                                  AccountRepository accountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.accountRepository = accountRepository;
    }

    public BalanceSheetView build(Long tenantId, LocalDate dateFrom, LocalDate dateTo) {
        List<JournalEntry> entries = journalEntryRepository
                .findByTenantIdAndEntryDateBetweenOrderByEntryDateAscIdAsc(tenantId, dateFrom, dateTo);

        BalanceSheetView view = new BalanceSheetView();
        view.setFormattedDateFrom(PdfFormatUtils.formatDate(dateFrom));
        view.setFormattedDateTo(PdfFormatUtils.formatDate(dateTo));

        view.setFormattedPeriodResult(PdfFormatUtils.formatMoney(zero()));
        if (entries.isEmpty()) {
            view.setFormattedTotalAssets(PdfFormatUtils.formatMoney(zero()));
            view.setFormattedTotalLiabilities(PdfFormatUtils.formatMoney(zero()));
            view.setFormattedTotalEquity(PdfFormatUtils.formatMoney(zero()));
            view.setFormattedPeriodResult(PdfFormatUtils.formatMoney(zero()));
            return view;
        }
        List<Long> entryIds = entries.stream().map(JournalEntry::getId).toList();
        List<JournalEntryLine> lines = journalEntryLineRepository.findByTenantIdAndJournalEntryIdIn(tenantId, entryIds);
        List<Account> orderedAccounts = accountRepository.findByTenantIdOrderByCodeAsc(tenantId);

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

        BalanceSheetSectionView assetSection = new BalanceSheetSectionView();
        assetSection.setSectionCode("ASSET");
        assetSection.setSectionLabel("Attività");

        BalanceSheetSectionView liabilitySection = new BalanceSheetSectionView();
        liabilitySection.setSectionCode("LIABILITY");
        liabilitySection.setSectionLabel("Passività");

        BalanceSheetSectionView equitySection = new BalanceSheetSectionView();
        equitySection.setSectionCode("EQUITY");
        equitySection.setSectionLabel("Patrimonio netto");

        BigDecimal totalAssets = zero();
        BigDecimal totalLiabilities = zero();
        BigDecimal totalEquity = zero();
        BigDecimal totalRevenue = zero();
        BigDecimal totalCost = zero();

        for (Account account : orderedAccounts) {
            BigDecimal balance = money(balancesByAccountId.getOrDefault(account.getId(), zero()));
            if (balance.compareTo(zero()) == 0) {
                continue;
            }

            if ("ASSET".equalsIgnoreCase(account.getAccountType())) {
                BigDecimal amount = balance;
                if (amount.compareTo(zero()) == 0) continue;

                BalanceSheetRowView row = new BalanceSheetRowView();
                row.setAccountId(account.getId());
                row.setAccountCode(account.getCode());
                row.setAccountName(account.getName());
                row.setFormattedAmount(PdfFormatUtils.formatMoney(amount));
                assetSection.getRows().add(row);

                totalAssets = money(totalAssets.add(amount));
            }

            if ("LIABILITY".equalsIgnoreCase(account.getAccountType())) {
                BigDecimal amount = money(balance.negate());
                if (amount.compareTo(zero()) == 0) continue;

                BalanceSheetRowView row = new BalanceSheetRowView();
                row.setAccountId(account.getId());
                row.setAccountCode(account.getCode());
                row.setAccountName(account.getName());
                row.setFormattedAmount(PdfFormatUtils.formatMoney(amount));
                liabilitySection.getRows().add(row);

                totalLiabilities = money(totalLiabilities.add(amount));
            }

            if ("EQUITY".equalsIgnoreCase(account.getAccountType())) {
                BigDecimal amount = money(balance.negate());
                if (amount.compareTo(zero()) == 0) continue;

                BalanceSheetRowView row = new BalanceSheetRowView();
                row.setAccountId(account.getId());
                row.setAccountCode(account.getCode());
                row.setAccountName(account.getName());
                row.setFormattedAmount(PdfFormatUtils.formatMoney(amount));
                equitySection.getRows().add(row);

                totalEquity = money(totalEquity.add(amount));
            }

            if ("REVENUE".equalsIgnoreCase(account.getAccountType())) {
                BigDecimal amount = money(balance.negate()); // ricavi mostrati positivi
                if (amount.compareTo(zero()) == 0) continue;
                totalRevenue = money(totalRevenue.add(amount));
            }

            if ("COST".equalsIgnoreCase(account.getAccountType())) {
                BigDecimal amount = balance; // costi già positivi
                if (amount.compareTo(zero()) == 0) continue;
                totalCost = money(totalCost.add(amount));
            }
        }

        BigDecimal periodResult = money(totalRevenue.subtract(totalCost));
        BigDecimal displayedEquity = money(totalEquity.add(periodResult));

        if (periodResult.compareTo(zero()) != 0) {
            BalanceSheetRowView resultRow = new BalanceSheetRowView();
            resultRow.setAccountId(null);
            resultRow.setAccountCode("");
            resultRow.setAccountName("Utile / perdita del periodo");
            resultRow.setFormattedAmount(PdfFormatUtils.formatMoney(periodResult));
            equitySection.getRows().add(resultRow);
        }

        assetSection.setFormattedTotal(PdfFormatUtils.formatMoney(totalAssets));
        liabilitySection.setFormattedTotal(PdfFormatUtils.formatMoney(totalLiabilities));
        equitySection.setFormattedTotal(PdfFormatUtils.formatMoney(displayedEquity));

        if (!assetSection.getRows().isEmpty()) {
            view.getSections().add(assetSection);
        }
        if (!liabilitySection.getRows().isEmpty()) {
            view.getSections().add(liabilitySection);
        }
        if (!equitySection.getRows().isEmpty()) {
            view.getSections().add(equitySection);
        }


        view.setFormattedPeriodResult(PdfFormatUtils.formatMoney(periodResult));
        view.setFormattedTotalEquity(PdfFormatUtils.formatMoney(displayedEquity));

        view.setFormattedTotalAssets(PdfFormatUtils.formatMoney(totalAssets));
        view.setFormattedTotalLiabilities(PdfFormatUtils.formatMoney(totalLiabilities));

        return view;
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? zero() : value.setScale(2, RoundingMode.HALF_UP);
    }
}
