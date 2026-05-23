package com.gestiva.accounting.v2.account.web;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AccountWebService {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    public AccountWebService(AccountRepository accountRepository,
                             JournalEntryRepository journalEntryRepository,
                             JournalEntryLineRepository journalEntryLineRepository) {

        this.accountRepository = accountRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
    }

    public List<AccountListItemView> findAll(Long tenantId) {
        List<Account> accounts = accountRepository.findByTenantIdOrderByCodeAsc(tenantId);

        Map<Long, Account> byId = new HashMap<>();
        for (Account account : accounts) {
            byId.put(account.getId(), account);
        }

        return accounts.stream().map(account -> {
            AccountListItemView v = new AccountListItemView();
            v.setId(account.getId());
            v.setCode(account.getCode());
            v.setName(account.getName());
            v.setAccountType(account.getAccountType());
            v.setNature(account.getNature());
            v.setLevelNo(account.getLevelNo());
            v.setLeafAccount(account.isLeafAccount());
            v.setSystemAccount(account.isSystemAccount());
            v.setActive(account.isActive());

            if (account.getParentId() != null) {
                Account parent = byId.get(account.getParentId());
                if (parent != null) {
                    v.setParentCode(parent.getCode());
                    v.setParentName(parent.getName());
                }
            }

            v.setIndentedName(indent(account.getLevelNo()) + account.getName());

            return v;
        }).toList();
    }

    private String indent(Integer levelNo) {
        if (levelNo == null || levelNo <= 1) {
            return "";
        }
        return "— ".repeat(levelNo - 1);
    }

    public List<AccountOptionView> findFinancialAccountOptions(Long tenantId) {
        return accountRepository.findByTenantIdOrderByCodeAsc(tenantId).stream()
                .filter(Account::isActive)
                .filter(Account::isLeafAccount)
                .map(account -> new AccountOptionView(
                        account.getId(),
                        account.getCode() + " - " + account.getName()
                ))
                .toList();
    }
    private boolean isFinancialAccount(com.gestiva.accounting.v2.account.entity.Account account) {
        if (account.getAccountType() == null) {
            return false;
        }
        String type = account.getAccountType().trim().toUpperCase();
        return "BANK".equals(type)
                || "CASH".equals(type)
                || "FINANCIAL".equals(type);
    }
    public java.util.List<AccountOptionView> findLeafOptions(Long tenantId) {
        return accountRepository.findByTenantIdAndLeafAccountTrueAndActiveTrueOrderByCodeAsc(tenantId)
                .stream()
                .map(account -> {
                    AccountOptionView v = new AccountOptionView();
                    v.setId(account.getId());
                    v.setCode(account.getCode());
                    v.setName(account.getName());
                    v.setLabel(account.getCode() + " - " + account.getName());
                    return v;
                })
                .toList();
    }
    public AccountLedgerView getLedger(Long tenantId, Long accountId) {
        var account = accountRepository.findByTenantIdAndId(tenantId, accountId)
                .orElseThrow(() -> new com.gestiva.common.exception.NotFoundException("Conto non trovato"));

        var lines = journalEntryLineRepository.findByTenantIdAndAccountIdOrderByJournalEntryIdAscLineNoAsc(tenantId, accountId);

        java.util.Map<Long, com.gestiva.accounting.v2.journal.entity.JournalEntry> entriesById = new java.util.HashMap<>();
        for (var line : lines) {
            journalEntryRepository.findByTenantIdAndId(tenantId, line.getJournalEntryId())
                    .ifPresent(entry -> entriesById.put(entry.getId(), entry));
        }

        java.math.BigDecimal totalDebit = java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal totalCredit = java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);

        AccountLedgerView v = new AccountLedgerView();
        v.setAccountId(account.getId());
        v.setCode(account.getCode());
        v.setName(account.getName());
        v.setAccountType(account.getAccountType());
        v.setNature(account.getNature());
        v.setActive(account.isActive());
        v.setSystemAccount(account.isSystemAccount());

        for (var line : lines) {
            var entry = entriesById.get(line.getJournalEntryId());

            totalDebit = totalDebit.add(zero(line.getDebitAmount()));
            totalCredit = totalCredit.add(zero(line.getCreditAmount()));

            AccountLedgerLineView lv = new AccountLedgerLineView();
            lv.setJournalEntryId(line.getJournalEntryId());
            lv.setJournalEntryNumber(entry != null ? entry.getEntryNumber() : "-");
            lv.setFormattedEntryDate(entry != null ? com.gestiva.documents.pdf.PdfFormatUtils.formatDate(entry.getEntryDate()) : "-");
            lv.setCausalCode(entry != null ? entry.getCausalCode() : "-");
            lv.setDescription(line.getDescription());
            lv.setFormattedDebitAmount(com.gestiva.documents.pdf.PdfFormatUtils.formatMoney(line.getDebitAmount()));
            lv.setFormattedCreditAmount(com.gestiva.documents.pdf.PdfFormatUtils.formatMoney(line.getCreditAmount()));
            v.getLines().add(lv);
        }

        java.math.BigDecimal balance;
        if ("DEBIT".equalsIgnoreCase(account.getNature())) {
            balance = totalDebit.subtract(totalCredit);
        } else {
            balance = totalCredit.subtract(totalDebit);
        }

        v.setFormattedTotalDebit(com.gestiva.documents.pdf.PdfFormatUtils.formatMoney(totalDebit));
        v.setFormattedTotalCredit(com.gestiva.documents.pdf.PdfFormatUtils.formatMoney(totalCredit));
        v.setFormattedBalance(com.gestiva.documents.pdf.PdfFormatUtils.formatMoney(balance));

        return v;
    }

    private java.math.BigDecimal zero(java.math.BigDecimal value) {
        return value == null
                ? java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP)
                : value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}