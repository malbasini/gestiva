package com.gestiva.accounting.v2.journal.service;

import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.accounting.v2.journal.web.JournalEntryForm;
import com.gestiva.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;

    public JournalEntryService(JournalEntryRepository journalEntryRepository,
                               JournalEntryLineRepository journalEntryLineRepository,
                               AccountRepository accountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.accountRepository = accountRepository;
    }
    public Long createManualEntry(Long tenantId, JournalEntryForm form) {
        return createEntryInternal(tenantId, form, null, null);
    }

    public Long createEntry(Long tenantId,
                            JournalEntryForm form,
                            String referenceType,
                            Long referenceId) {
        return createEntryInternal(tenantId, form, referenceType, referenceId);
    }

    private Long createEntryInternal(Long tenantId,
                                     JournalEntryForm form,
                                     String referenceType,
                                     Long referenceId) {
        List<JournalEntryLine> preparedLines = new ArrayList<>();

        BigDecimal totalDebit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalCredit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (form.getLines() == null || form.getLines().isEmpty()) {
            throw new BusinessException("La scrittura deve contenere almeno una riga.");
        }

        int lineNo = 1;
        for (var lineForm : form.getLines()) {
            if (lineForm.getAccountId() == null &&
                    isBlank(lineForm.getDescription()) &&
                    lineForm.getDebitAmount() == null &&
                    lineForm.getCreditAmount() == null) {
                continue;
            }

            if (lineForm.getAccountId() == null) {
                throw new BusinessException("Ogni riga deve avere un conto.");
            }

            var account = accountRepository.findByTenantIdAndId(tenantId, lineForm.getAccountId())
                    .orElseThrow(() -> new BusinessException("Conto non trovato."));

            if (!account.isLeafAccount()) {
                throw new BusinessException("È possibile registrare movimenti solo su conti foglia.");
            }

            BigDecimal debit = scale(lineForm.getDebitAmount());
            BigDecimal credit = scale(lineForm.getCreditAmount());

            boolean hasDebit = debit.compareTo(BigDecimal.ZERO) > 0;
            boolean hasCredit = credit.compareTo(BigDecimal.ZERO) > 0;

            if (hasDebit == hasCredit) {
                throw new BusinessException("Ogni riga deve avere solo Dare o solo Avere.");
            }

            JournalEntryLine line = new JournalEntryLine();
            line.setTenantId(tenantId);
            line.setLineNo(lineNo++);
            line.setAccountId(account.getId());
            line.setDescription(
                    !isBlank(lineForm.getDescription()) ? lineForm.getDescription() : form.getDescription()
            );
            line.setDebitAmount(debit);
            line.setCreditAmount(credit);

            preparedLines.add(line);
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }

        if (preparedLines.isEmpty()) {
            throw new BusinessException("La scrittura deve contenere almeno una riga valorizzata.");
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException("La scrittura non è quadrata: totale Dare diverso da totale Avere.");
        }

        JournalEntry entry = new JournalEntry();
        entry.setTenantId(tenantId);
        entry.setEntryNumber(nextEntryNumber(tenantId));
        entry.setEntryDate(form.getEntryDate());
        entry.setCausalCode(form.getCausalCode());
        entry.setDescription(form.getDescription());
        entry.setReferenceType(referenceType);
        entry.setReferenceId(referenceId);
        entry.setCurrencyCode(form.getCurrencyCode());
        entry.setTotalDebit(totalDebit);
        entry.setTotalCredit(totalCredit);
        entry.setPosted(true);
        entry.setNotes(form.getNotes());

        JournalEntry saved = journalEntryRepository.save(entry);

        for (JournalEntryLine line : preparedLines) {
            line.setJournalEntryId(saved.getId());
            journalEntryLineRepository.save(line);
        }

        return saved.getId();
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
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}