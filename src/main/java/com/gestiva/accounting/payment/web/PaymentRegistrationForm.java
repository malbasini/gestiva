package com.gestiva.accounting.payment.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class PaymentRegistrationForm {

    @NotNull(message = "Scadenza non valida.")
    private Long paymentDueId;

    @NotNull(message = "Inserisci la data.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paymentDate;

    @NotBlank(message = "Inserisci l'importo.")
    private String amount;

    @Size(max = 30, message = "Il metodo di pagamento non può superare 30 caratteri.")
    private String paymentMethod;

    @Size(max = 100, message = "Il riferimento non può superare 100 caratteri.")
    private String reference;

    @Size(max = 1000, message = "Le note non possono superare 1000 caratteri.")
    private String notes;

    @NotNull(message = "Seleziona il conto finanziario.")
    private Long financialAccountId;

    public Long getFinancialAccountId() {
        return financialAccountId;
    }

    public void setFinancialAccountId(Long financialAccountId) {
        this.financialAccountId = financialAccountId;
    }

    public Long getPaymentDueId() { return paymentDueId; }
    public void setPaymentDueId(Long paymentDueId) { this.paymentDueId = paymentDueId; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}