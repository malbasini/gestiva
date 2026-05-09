package com.gestiva.accounting.entry.web;

import com.gestiva.accounting.entry.repository.AccountingEntryLineRepository;
import com.gestiva.accounting.entry.repository.AccountingEntryRepository;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}