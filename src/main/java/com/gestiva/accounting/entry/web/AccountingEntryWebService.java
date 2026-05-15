package com.gestiva.accounting.entry.web;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.web.PaymentDueListItemView;
import com.gestiva.accounting.entry.entity.AccountingEntry;
import com.gestiva.accounting.entry.repository.AccountingEntryLineRepository;
import com.gestiva.accounting.entry.repository.AccountingEntryRepository;
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
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountingEntryWebService {

    private final AccountingEntryRepository accountingEntryRepository;
    private final AccountingEntryLineRepository accountingEntryLineRepository;

    public AccountingEntryWebService(AccountingEntryRepository accountingEntryRepository,
                                     AccountingEntryLineRepository accountingEntryLineRepository) {
        this.accountingEntryRepository = accountingEntryRepository;
        this.accountingEntryLineRepository = accountingEntryLineRepository;
    }

    public List<AccountingEntryListItemView> findAll(Long tenantId) {
        return accountingEntryRepository.findByTenantIdOrderByEntryDateDescIdDesc(tenantId)
                .stream()
                .map(entry -> {
                    AccountingEntryListItemView v = new AccountingEntryListItemView();
                    v.setId(entry.getId());
                    v.setEntryNumber(entry.getEntryNumber());
                    v.setFormattedEntryDate(PdfFormatUtils.formatDate(entry.getEntryDate()));
                    v.setCausalCode(entry.getCausalCode());
                    v.setDescription(entry.getDescription());
                    v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(entry.getTotalAmount()));
                    v.setCurrencyCode(entry.getCurrencyCode());
                    v.setReferenceType(entry.getReferenceType());
                    v.setReferenceId(entry.getReferenceId());
                    return v;
                })
                .toList();
    }

    public AccountingEntryDetailView getDetail(Long tenantId, Long id) {
        var entry = accountingEntryRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Movimento di prima nota non trovato"));

        var lines = accountingEntryLineRepository.findByTenantIdAndAccountingEntryIdOrderByLineNoAsc(tenantId, id);

        AccountingEntryDetailView v = new AccountingEntryDetailView();
        v.setId(entry.getId());
        v.setEntryNumber(entry.getEntryNumber());
        v.setFormattedEntryDate(PdfFormatUtils.formatDate(entry.getEntryDate()));
        v.setCausalCode(entry.getCausalCode());
        v.setDescription(entry.getDescription());
        v.setCurrencyCode(entry.getCurrencyCode());
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(entry.getTotalAmount()));
        v.setNotes(entry.getNotes());
        v.setReferenceType(entry.getReferenceType());
        v.setReferenceId(entry.getReferenceId());

        for (var line : lines) {
            AccountingEntryLineView lv = new AccountingEntryLineView();
            lv.setLineNo(line.getLineNo());
            lv.setLineType(line.getLineType());
            lv.setDescription(line.getDescription());
            lv.setFormattedAmount(PdfFormatUtils.formatMoney(line.getAmount()));
            v.getLines().add(lv);
        }

        return v;
    }

    public Page<AccountingEntryListItemView> findPage(Long tenantId,
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
        Specification<AccountingEntry> spec = Specification.where(byTenant(tenantId))
                .and(byCausalCode(causalCode))
                .and(bySearch(q))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));
        return accountingEntryRepository.findAll(spec, pageable).map(this::toListItemView);

    }

    private Specification<AccountingEntry> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    private Specification<AccountingEntry> byCausalCode(String causalCode) {
        if (causalCode == null || causalCode.trim().isEmpty()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("causalCode"), causalCode);
    }

    private Specification<AccountingEntry> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("entryNumber")), like);
    }

    private Specification<AccountingEntry> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("entryDate"), dateFrom);
    }

    private Specification<AccountingEntry> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("entryDate"), dateTo);
    }

    private AccountingEntryListItemView toListItemView(AccountingEntry entry) {
        AccountingEntryListItemView v = new AccountingEntryListItemView();
        v.setId(entry.getId());
        v.setEntryNumber(entry.getEntryNumber());
        v.setFormattedEntryDate(PdfFormatUtils.formatDate(entry.getEntryDate()));
        v.setCausalCode(entry.getCausalCode());
        v.setDescription(entry.getDescription());
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(entry.getTotalAmount()));
        v.setCurrencyCode(entry.getCurrencyCode());
        v.setReferenceType(entry.getReferenceType());
        return v;
    }



}