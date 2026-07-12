package com.gestiva.accounting.v2.journal.service;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
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
    private final DocumentSequenceService documentSequenceService;
    private final InvoiceRepository invoiceRepository;




    public JournalAutoPostingService(JournalEntryRepository journalEntryRepository,
                                     JournalEntryLineRepository journalEntryLineRepository,
                                     AccountRepository accountRepository,
                                     DocumentSequenceService documentSequenceService,
                                     InvoiceRepository invoiceRepository) {

        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.accountRepository = accountRepository;
        this.documentSequenceService = documentSequenceService;
        this.invoiceRepository = invoiceRepository;
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
        return documentSequenceService.nextJournalEntryNumber(tenantId);
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null
                ? zero()
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public void postCustomerInvoice(Long tenantId,
                                    LocalDate entryDate,
                                    String documentNumber,
                                    BigDecimal taxableAmount,
                                    BigDecimal taxAmount,
                                    BigDecimal totalAmount,
                                    String currencyCode,
                                    Long invoiceId,
                                    String notes) {

        BigDecimal imponibile = scale(taxableAmount);
        BigDecimal iva = scale(taxAmount);
        BigDecimal totale = scale(totalAmount);

        Account customerReceivables = requireAccount(tenantId, "1210");
        Account salesRevenue = requireAccount(tenantId, "4100");
        Account vatOutput = requireAccount(tenantId, "2210");

        JournalEntry entry = new JournalEntry();
        entry.setTenantId(tenantId);
        entry.setEntryNumber(nextEntryNumber(tenantId));
        entry.setEntryDate(entryDate);
        entry.setCausalCode("SALES_INVOICE");
        entry.setDescription("Fattura cliente " + documentNumber);
        entry.setReferenceType("CUSTOMER_INVOICE");
        entry.setReferenceId(invoiceId);
        entry.setCurrencyCode(currencyCode);
        entry.setTotalDebit(totale);
        entry.setTotalCredit(totale);
        entry.setPosted(true);
        entry.setNotes(notes);

        JournalEntry saved = journalEntryRepository.save(entry);

        JournalEntryLine line1 = new JournalEntryLine();
        line1.setTenantId(tenantId);
        line1.setJournalEntryId(saved.getId());
        line1.setLineNo(1);
        line1.setAccountId(customerReceivables.getId());
        line1.setDescription("Rilevazione credito cliente");
        line1.setDebitAmount(totale);
        line1.setCreditAmount(zero());

        JournalEntryLine line2 = new JournalEntryLine();
        line2.setTenantId(tenantId);
        line2.setJournalEntryId(saved.getId());
        line2.setLineNo(2);
        line2.setAccountId(salesRevenue.getId());
        line2.setDescription("Ricavi da vendite");
        line2.setDebitAmount(zero());
        line2.setCreditAmount(imponibile);

        JournalEntryLine line3 = new JournalEntryLine();
        line3.setTenantId(tenantId);
        line3.setJournalEntryId(saved.getId());
        line3.setLineNo(3);
        line3.setAccountId(vatOutput.getId());
        line3.setDescription("IVA a debito");
        line3.setDebitAmount(zero());
        line3.setCreditAmount(iva);
        Invoice invoice = invoiceRepository.findByTenantIdAndId(tenantId, invoiceId).orElse(null);
        if (invoice != null) {
            invoice.setStatus("ISSUED");
            invoiceRepository.save(invoice);
        }
        journalEntryLineRepository.save(line1);
        journalEntryLineRepository.save(line2);
        journalEntryLineRepository.save(line3);

    }

    public Long postSupplierInvoice(Long tenantId,
                                    LocalDate entryDate,
                                    String documentNumber,
                                    BigDecimal taxableAmount,
                                    BigDecimal taxAmount,
                                    BigDecimal totalAmount,
                                    String currencyCode,
                                    Long supplierInvoiceId,
                                    String notes) {

        BigDecimal imponibile = scale(taxableAmount);
        BigDecimal iva = scale(taxAmount);
        BigDecimal totale = scale(totalAmount);

        Account purchaseCosts = requireAccount(tenantId, "5100");
        Account vatInput = requireAccount(tenantId, "2220");
        Account supplierPayables = requireAccount(tenantId, "2110");

        JournalEntry entry = new JournalEntry();
        entry.setTenantId(tenantId);
        entry.setEntryNumber(nextEntryNumber(tenantId));
        entry.setEntryDate(entryDate);
        entry.setCausalCode("PURCHASE_INVOICE");
        entry.setDescription("Fattura fornitore " + documentNumber);
        entry.setReferenceType("SUPPLIER_INVOICE");
        entry.setReferenceId(supplierInvoiceId);
        entry.setCurrencyCode(currencyCode);
        entry.setTotalDebit(totale);
        entry.setTotalCredit(totale);
        entry.setPosted(true);
        entry.setNotes(notes);

        JournalEntry saved = journalEntryRepository.save(entry);

        JournalEntryLine line1 = new JournalEntryLine();
        line1.setTenantId(tenantId);
        line1.setJournalEntryId(saved.getId());
        line1.setLineNo(1);
        line1.setAccountId(purchaseCosts.getId());
        line1.setDescription("Rilevazione costo acquisto");
        line1.setDebitAmount(imponibile);
        line1.setCreditAmount(zero());

        JournalEntryLine line2 = new JournalEntryLine();
        line2.setTenantId(tenantId);
        line2.setJournalEntryId(saved.getId());
        line2.setLineNo(2);
        line2.setAccountId(vatInput.getId());
        line2.setDescription("IVA a credito");
        line2.setDebitAmount(iva);
        line2.setCreditAmount(zero());

        JournalEntryLine line3 = new JournalEntryLine();
        line3.setTenantId(tenantId);
        line3.setJournalEntryId(saved.getId());
        line3.setLineNo(3);
        line3.setAccountId(supplierPayables.getId());
        line3.setDescription("Rilevazione debito fornitore");
        line3.setDebitAmount(zero());
        line3.setCreditAmount(totale);

        journalEntryLineRepository.save(line1);
        journalEntryLineRepository.save(line2);
        journalEntryLineRepository.save(line3);

        return saved.getId();
    }











}