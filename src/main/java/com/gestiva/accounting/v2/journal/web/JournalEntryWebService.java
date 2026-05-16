package com.gestiva.accounting.v2.journal.web;

import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class JournalEntryWebService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;

    public JournalEntryWebService(JournalEntryRepository journalEntryRepository,
                                  JournalEntryLineRepository journalEntryLineRepository,
                                  AccountRepository accountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.accountRepository = accountRepository;
    }

    public List<JournalEntryListItemView> findAll(Long tenantId) {
        return journalEntryRepository.findByTenantIdOrderByEntryDateDescIdDesc(tenantId)
                .stream()
                .map(entry -> {
                    JournalEntryListItemView v = new JournalEntryListItemView();
                    v.setId(entry.getId());
                    v.setEntryNumber(entry.getEntryNumber());
                    v.setFormattedEntryDate(PdfFormatUtils.formatDate(entry.getEntryDate()));
                    v.setCausalCode(entry.getCausalCode());
                    v.setDescription(entry.getDescription());
                    v.setFormattedTotalDebit(PdfFormatUtils.formatMoney(entry.getTotalDebit()));
                    v.setFormattedTotalCredit(PdfFormatUtils.formatMoney(entry.getTotalCredit()));
                    v.setCurrencyCode(entry.getCurrencyCode());
                    v.setPosted(entry.isPosted());
                    v.setReferenceType(entry.getReferenceType());
                    v.setReferenceId(entry.getReferenceId());
                    return v;
                })
                .toList();
    }

    public JournalEntryDetailView getDetail(Long tenantId, Long id) {
        var entry = journalEntryRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Scrittura contabile V2 non trovata"));

        var lines = journalEntryLineRepository.findByTenantIdAndJournalEntryIdOrderByLineNoAsc(tenantId, id);

        Map<Long, com.gestiva.accounting.v2.account.entity.Account> accountsById = new HashMap<>();
        for (var line : lines) {
            accountRepository.findByTenantIdAndId(tenantId, line.getAccountId())
                    .ifPresent(account -> accountsById.put(account.getId(), account));
        }

        JournalEntryDetailView v = new JournalEntryDetailView();
        v.setId(entry.getId());
        v.setEntryNumber(entry.getEntryNumber());
        v.setFormattedEntryDate(PdfFormatUtils.formatDate(entry.getEntryDate()));
        v.setCausalCode(entry.getCausalCode());
        v.setDescription(entry.getDescription());
        v.setCurrencyCode(entry.getCurrencyCode());
        v.setFormattedTotalDebit(PdfFormatUtils.formatMoney(entry.getTotalDebit()));
        v.setFormattedTotalCredit(PdfFormatUtils.formatMoney(entry.getTotalCredit()));
        v.setPosted(entry.isPosted());
        v.setNotes(entry.getNotes());
        v.setReferenceType(entry.getReferenceType());
        v.setReferenceId(entry.getReferenceId());

        for (var line : lines) {
            var account = accountsById.get(line.getAccountId());

            JournalEntryDetailLineView lv = new JournalEntryDetailLineView();
            lv.setLineNo(line.getLineNo());
            lv.setAccountCode(account != null ? account.getCode() : "-");
            lv.setAccountName(account != null ? account.getName() : "-");
            lv.setDescription(line.getDescription());
            lv.setFormattedDebitAmount(PdfFormatUtils.formatMoney(line.getDebitAmount()));
            lv.setFormattedCreditAmount(PdfFormatUtils.formatMoney(line.getCreditAmount()));
            lv.setAccountId(line.getAccountId());
            v.getLines().add(lv);
        }

        return v;
    }

    public Page<JournalEntryDetailView> findPage(Long tenantId,
                                                   int page,
                                                   int size,
                                                   String causalCode,
                                                   String q,
                                                   LocalDate dateFrom,
                                                   LocalDate dateTo) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("entryDate"), Sort.Order.desc("id"))
        );
        Specification<JournalEntry> spec = Specification.where(byTenant(tenantId))
                .and(byCausalCode(causalCode))
                .and(bySearch(q))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));
        return journalEntryRepository.findAll(spec, pageable).map(this::toListItemView);

    }

    private Specification<JournalEntry> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    private Specification<JournalEntry> byCausalCode(String causalCode) {
        if (causalCode == null ||causalCode.trim().isEmpty()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("causalCode"), causalCode);
    }

    private Specification<JournalEntry> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("entryNumber")), like);
    }

    private Specification<JournalEntry> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("entryDate"), dateFrom);
    }

    private Specification<JournalEntry> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("entryDate"), dateTo);
    }

    private JournalEntryDetailView toListItemView(JournalEntry entry) {
        JournalEntryDetailView v = new JournalEntryDetailView();
        v.setId(entry.getId());
        v.setEntryNumber(entry.getEntryNumber());
        v.setFormattedEntryDate(PdfFormatUtils.formatDate(entry.getEntryDate()));
        v.setCausalCode(entry.getCausalCode());
        v.setDescription(entry.getDescription());
        v.setFormattedTotalCredit(PdfFormatUtils.formatMoney(entry.getTotalCredit()));
        v.setFormattedTotalDebit(PdfFormatUtils.formatMoney(entry.getTotalDebit()));
        v.setPosted(entry.isPosted());
        return v;
    }
}