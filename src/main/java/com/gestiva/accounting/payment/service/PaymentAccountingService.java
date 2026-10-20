package com.gestiva.accounting.payment.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.service.JournalEntryService;
import com.gestiva.accounting.v2.journal.web.JournalEntryForm;
import com.gestiva.accounting.v2.journal.web.JournalEntryLineForm;
import com.gestiva.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.gestiva.security.company.context.CompanyContext;

@Service
@Transactional
public class PaymentAccountingService {

    private static final String CUSTOMER_RECEIVABLE_ACCOUNT_CODE = "1210";
    private static final String SUPPLIER_PAYABLE_ACCOUNT_CODE = "2110";

    private final JournalEntryService journalEntryService;
    private final AccountRepository accountRepository;
    private final CompanyContext companyContext;

    public PaymentAccountingService(
            JournalEntryService journalEntryService,
            AccountRepository accountRepository,
            CompanyContext companyContext) {

        this.journalEntryService = journalEntryService;
        this.accountRepository = accountRepository;
        this.companyContext = companyContext;
    }

    public Long createAccountingEntryForPayment(Long tenantId,
                                                PaymentDue due,
                                                LocalDate paymentDate,
                                                BigDecimal amount,
                                                Long financialAccountId,
                                                String reference,
                                                String notes) {

        if (financialAccountId == null) {
            throw new BusinessException("Seleziona il conto finanziario.");
        }

        Long tradeAccountId = resolveTradeAccountId(tenantId, due);

        JournalEntryForm form = new JournalEntryForm();
        form.setEntryDate(paymentDate);
        form.setCurrencyCode(due.getCurrencyCode() != null ? due.getCurrencyCode() : "EUR");
        form.setNotes(notes);

        if ("RECEIVABLE".equalsIgnoreCase(due.getDirection())) {
            form.setCausalCode("CUSTOMER_RECEIPT");
            form.setDescription(buildDescription("Incasso", due, reference));
            form.setLines(buildCustomerReceiptLines(
                    financialAccountId,
                    tradeAccountId,
                    amount,
                    form.getDescription()
            ));
        } else if ("PAYABLE".equalsIgnoreCase(due.getDirection())) {
            form.setCausalCode("SUPPLIER_PAYMENT");
            form.setDescription(buildDescription("Pagamento", due, reference));
            form.setLines(buildSupplierPaymentLines(
                    financialAccountId,
                    tradeAccountId,
                    amount,
                    form.getDescription()
            ));
        } else {
            throw new BusinessException("Direzione scadenza non valida: " + due.getDirection());
        }

        return journalEntryService.createEntry(
                tenantId,
                form,
                due.getReferenceType(),
                due.getReferenceId()
        );
    }

    private Long resolveTradeAccountId(Long tenantId, PaymentDue due) {

        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata."
            );
        }
        if (!companyId.equals(due.getCompanyId())) {
            throw new BusinessException(
                    "La scadenza non appartiene alla società attiva."
            );
        }
        String accountCode;

        if ("RECEIVABLE".equalsIgnoreCase(due.getDirection())) {
            accountCode = CUSTOMER_RECEIVABLE_ACCOUNT_CODE;

        } else if ("PAYABLE".equalsIgnoreCase(due.getDirection())) {
            accountCode = SUPPLIER_PAYABLE_ACCOUNT_CODE;

        } else {
            throw new BusinessException(
                    "Direzione scadenza non valida: " + due.getDirection()
            );
        }

        return accountRepository
                .findByTenantIdAndCompanyIdAndCode(
                        tenantId,
                        companyId,
                        accountCode
                )
                .orElseThrow(() -> new BusinessException(
                        "Conto " + accountCode
                                + " non trovato nella società attiva."
                ))
                .getId();
    }
    private List<JournalEntryLineForm> buildCustomerReceiptLines(Long financialAccountId,
                                                                 Long receivableAccountId,
                                                                 BigDecimal amount,
                                                                 String description) {
        List<JournalEntryLineForm> lines = new ArrayList<>();

        JournalEntryLineForm debitLine = new JournalEntryLineForm();
        debitLine.setAccountId(financialAccountId);
        debitLine.setDescription(description);
        debitLine.setDebitAmount(amount);
        debitLine.setCreditAmount(BigDecimal.ZERO);
        lines.add(debitLine);

        JournalEntryLineForm creditLine = new JournalEntryLineForm();
        creditLine.setAccountId(receivableAccountId);
        creditLine.setDescription(description);
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(amount);
        lines.add(creditLine);

        return lines;
    }

    private List<JournalEntryLineForm> buildSupplierPaymentLines(Long financialAccountId,
                                                                 Long payableAccountId,
                                                                 BigDecimal amount,
                                                                 String description) {
        List<JournalEntryLineForm> lines = new ArrayList<>();

        JournalEntryLineForm debitLine = new JournalEntryLineForm();
        debitLine.setAccountId(payableAccountId);
        debitLine.setDescription(description);
        debitLine.setDebitAmount(amount);
        debitLine.setCreditAmount(BigDecimal.ZERO);
        lines.add(debitLine);

        JournalEntryLineForm creditLine = new JournalEntryLineForm();
        creditLine.setAccountId(financialAccountId);
        creditLine.setDescription(description);
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(amount);
        lines.add(creditLine);

        return lines;
    }

    private String buildDescription(String operation, PaymentDue due, String reference) {
        String base = operation + " scadenza " + due.getDocumentNumber();
        if (reference != null && !reference.isBlank()) {
            return base + " - Rif. " + reference;
        }
        return base;
    }
}