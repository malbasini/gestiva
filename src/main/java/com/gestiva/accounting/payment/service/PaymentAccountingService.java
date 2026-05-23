package com.gestiva.accounting.payment.service;

import com.gestiva.accounting.due.entity.PaymentDue;
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

@Service
@Transactional
public class PaymentAccountingService {

    private final JournalEntryService journalEntryService;

    public PaymentAccountingService(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    public Long createAccountingEntryForPayment(Long tenantId,
                                                PaymentDue due,
                                                LocalDate paymentDate,
                                                BigDecimal amount,
                                                Long financialAccountId,
                                                String reference,
                                                String notes,
                                                Long customerReceivableAccountId,
                                                Long supplierPayableAccountId){

        if (financialAccountId == null) {
          throw new BusinessException("Seleziona il conto finanziario.");
        }

        Long tradeAccountId = resolveTradeAccountId(
                due,
                customerReceivableAccountId,
                supplierPayableAccountId
        );

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

    private Long resolveTradeAccountId(PaymentDue due,
                                       Long customerReceivableAccountId,
                                       Long supplierPayableAccountId) {
        if ("CUSTOMER".equalsIgnoreCase(due.getPartyType())) {
            if (customerReceivableAccountId == null) {
                throw new BusinessException("Configura il conto crediti verso clienti.");
            }
            return customerReceivableAccountId;
        }

        if ("SUPPLIER".equalsIgnoreCase(due.getPartyType())) {
            if (supplierPayableAccountId == null) {
                throw new BusinessException("Configura il conto debiti verso fornitori.");
            }
            return supplierPayableAccountId;
        }

        throw new BusinessException("Tipo soggetto non valido: " + due.getPartyType());
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