package com.gestiva.accounting.v2.journal.service;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Transactional
public class JournalAutoPostingService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;

    public JournalAutoPostingService(JournalEntryRepository journalEntryRepository,
                                     JournalEntryLineRepository journalEntryLineRepository,
                                     AccountRepository accountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.accountRepository = accountRepository;
    }

    public Long postCustomerReceipt(Long tenantId,
                                    LocalDate entryDate,
                                    String documentNumber,
                                    BigDecimal amount,
                                    String currencyCode,
                                    Long paymentDueId,
                                    String notes) {

        BigDecimal value = scale(amount);

        Account bank = requireAccount(tenantId, "1120");
        Account customerReceivables = requireAccount(tenantId, "1210");

        JournalEntry entry = new JournalEntry();
        entry.setTenantId(tenantId);
        entry.setEntryNumber(nextEntryNumber(tenantId));
        entry.setEntryDate(entryDate);
        entry.setCausalCode("CUSTOMER_RECEIPT");
        entry.setDescription("Incasso cliente su scadenza " + documentNumber);
        entry.setReferenceType("PAYMENT_DUE");
        entry.setReferenceId(paymentDueId);
        entry.setCurrencyCode(currencyCode);
        entry.setTotalDebit(value);
        entry.setTotalCredit(value);
        entry.setPosted(true);
        entry.setNotes(notes);

        JournalEntry saved = journalEntryRepository.save(entry);

        JournalEntryLine line1 = new JournalEntryLine();
        line1.setTenantId(tenantId);
        line1.setJournalEntryId(saved.getId());
        line1.setLineNo(1);
        line1.setAccountId(bank.getId());
        line1.setDescription("Incasso su banca");
        line1.setDebitAmount(value);
        line1.setCreditAmount(zero());

        JournalEntryLine line2 = new JournalEntryLine();
        line2.setTenantId(tenantId);
        line2.setJournalEntryId(saved.getId());
        line2.setLineNo(2);
        line2.setAccountId(customerReceivables.getId());
        line2.setDescription("Chiusura credito cliente");
        line2.setDebitAmount(zero());
        line2.setCreditAmount(value);

        journalEntryLineRepository.save(line1);
        journalEntryLineRepository.save(line2);

        return saved.getId();
    }

    public Long postSupplierPayment(Long tenantId,
                                    LocalDate entryDate,
                                    String documentNumber,
                                    BigDecimal amount,
                                    String currencyCode,
                                    Long paymentDueId,
                                    String notes) {

        BigDecimal value = scale(amount);

        Account bank = requireAccount(tenantId, "1120");
        Account supplierPayables = requireAccount(tenantId, "2110");

        JournalEntry entry = new JournalEntry();
        entry.setTenantId(tenantId);
        entry.setEntryNumber(nextEntryNumber(tenantId));
        entry.setEntryDate(entryDate);
        entry.setCausalCode("SUPPLIER_PAYMENT");
        entry.setDescription("Pagamento fornitore su scadenza " + documentNumber);
        entry.setReferenceType("PAYMENT_DUE");
        entry.setReferenceId(paymentDueId);
        entry.setCurrencyCode(currencyCode);
        entry.setTotalDebit(value);
        entry.setTotalCredit(value);
        entry.setPosted(true);
        entry.setNotes(notes);

        JournalEntry saved = journalEntryRepository.save(entry);

        JournalEntryLine line1 = new JournalEntryLine();
        line1.setTenantId(tenantId);
        line1.setJournalEntryId(saved.getId());
        line1.setLineNo(1);
        line1.setAccountId(supplierPayables.getId());
        line1.setDescription("Chiusura debito fornitore");
        line1.setDebitAmount(value);
        line1.setCreditAmount(zero());

        JournalEntryLine line2 = new JournalEntryLine();
        line2.setTenantId(tenantId);
        line2.setJournalEntryId(saved.getId());
        line2.setLineNo(2);
        line2.setAccountId(bank.getId());
        line2.setDescription("Pagamento da banca");
        line2.setDebitAmount(zero());
        line2.setCreditAmount(value);

        journalEntryLineRepository.save(line1);
        journalEntryLineRepository.save(line2);

        return saved.getId();
    }

    private Account requireAccount(Long tenantId, String code) {
        return accountRepository.findByTenantIdAndCode(tenantId, code)
                .orElseThrow(() -> new BusinessException("Conto contabile V2 non trovato: " + code));
    }

    private String nextEntryNumber(Long tenantId) {
        long next = journalEntryRepository.count() + 1;
        String number = "JE-" + String.format("%05d", next);

        while (journalEntryRepository.existsByTenantIdAndEntryNumber(tenantId, number)) {
            next++;
            number = "JE-" + String.format("%05d", next);
        }
        return number;
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null
                ? zero()
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}