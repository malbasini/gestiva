package com.gestiva.accounting.entry.service;

import com.gestiva.accounting.entry.entity.AccountingEntry;
import com.gestiva.accounting.entry.entity.AccountingEntryLine;
import com.gestiva.accounting.entry.repository.AccountingEntryLineRepository;
import com.gestiva.accounting.entry.repository.AccountingEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Transactional
public class AccountingEntryService {

    private final AccountingEntryRepository accountingEntryRepository;
    private final AccountingEntryLineRepository accountingEntryLineRepository;

    public AccountingEntryService(AccountingEntryRepository accountingEntryRepository,
                                  AccountingEntryLineRepository accountingEntryLineRepository) {
        this.accountingEntryRepository = accountingEntryRepository;
        this.accountingEntryLineRepository = accountingEntryLineRepository;
    }

    public Long registerCustomerReceipt(Long tenantId,
                                        LocalDate entryDate,
                                        String description,
                                        String currencyCode,
                                        BigDecimal amount,
                                        String referenceType,
                                        Long referenceId,
                                        String notes) {
        return createSingleLineEntry(
                tenantId,
                entryDate,
                "CUSTOMER_RECEIPT",
                description,
                "INCOME",
                currencyCode,
                amount,
                referenceType,
                referenceId,
                notes
        );
    }

    public Long registerSupplierPayment(Long tenantId,
                                        LocalDate entryDate,
                                        String description,
                                        String currencyCode,
                                        BigDecimal amount,
                                        String referenceType,
                                        Long referenceId,
                                        String notes) {
        return createSingleLineEntry(
                tenantId,
                entryDate,
                "SUPPLIER_PAYMENT",
                description,
                "EXPENSE",
                currencyCode,
                amount,
                referenceType,
                referenceId,
                notes
        );
    }

    public Long registerManualIncome(Long tenantId,
                                     LocalDate entryDate,
                                     String description,
                                     String currencyCode,
                                     BigDecimal amount,
                                     String notes) {
        return createSingleLineEntry(
                tenantId,
                entryDate,
                "MANUAL_INCOME",
                description,
                "INCOME",
                currencyCode,
                amount,
                null,
                null,
                notes
        );
    }

    public Long registerManualExpense(Long tenantId,
                                      LocalDate entryDate,
                                      String description,
                                      String currencyCode,
                                      BigDecimal amount,
                                      String notes) {
        return createSingleLineEntry(
                tenantId,
                entryDate,
                "MANUAL_EXPENSE",
                description,
                "EXPENSE",
                currencyCode,
                amount,
                null,
                null,
                notes
        );
    }

    private Long createSingleLineEntry(Long tenantId,
                                       LocalDate entryDate,
                                       String causalCode,
                                       String description,
                                       String lineType,
                                       String currencyCode,
                                       BigDecimal amount,
                                       String referenceType,
                                       Long referenceId,
                                       String notes) {

        BigDecimal scaledAmount = scale(amount);

        AccountingEntry entry = new AccountingEntry();
        entry.setTenantId(tenantId);
        entry.setEntryNumber(nextEntryNumber(tenantId));
        entry.setEntryDate(entryDate);
        entry.setCausalCode(causalCode);
        entry.setDescription(description);
        entry.setReferenceType(referenceType);
        entry.setReferenceId(referenceId);
        entry.setCurrencyCode(currencyCode);
        entry.setTotalAmount(scaledAmount);
        entry.setNotes(notes);

        AccountingEntry saved = accountingEntryRepository.save(entry);

        AccountingEntryLine line = new AccountingEntryLine();
        line.setTenantId(tenantId);
        line.setAccountingEntryId(saved.getId());
        line.setLineNo(1);
        line.setLineType(lineType);
        line.setDescription(description);
        line.setAmount(scaledAmount);

        accountingEntryLineRepository.save(line);

        return saved.getId();
    }

    private String nextEntryNumber(Long tenantId) {
        long next = accountingEntryRepository.count() + 1;
        String number = "PN-" + String.format("%05d", next);

        while (accountingEntryRepository.existsByTenantIdAndEntryNumber(tenantId, number)) {
            next++;
            number = "PN-" + String.format("%05d", next);
        }
        return number;
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}
