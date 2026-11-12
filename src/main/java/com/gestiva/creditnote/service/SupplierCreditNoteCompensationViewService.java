
package com.gestiva.creditnote.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.creditnote.repository.CreditNoteFinancialPositionRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.security.company.context.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SupplierCreditNoteCompensationViewService {

    private final CompanyContext companyContext;
    private final CreditNoteRepository notes;
    private final CreditNoteFinancialPositionRepository positions;
    private final PaymentDueRepository dues;

    public SupplierCreditNoteCompensationViewService(
            CompanyContext companyContext,
            CreditNoteRepository notes,
            CreditNoteFinancialPositionRepository positions,
            PaymentDueRepository dues) {

        this.companyContext = companyContext;
        this.notes = notes;
        this.positions = positions;
        this.dues = dues;
    }

    public record CompensationView(
            CreditNoteFinancialPosition position,
            List<PaymentDue> eligibleDues,
            boolean compensable
    ) {}

    public CompensationView getView(
            Long tenantId,
            Long creditNoteId) {

        if (tenantId == null || creditNoteId == null
                || tenantId <= 0 || creditNoteId <= 0) {
            throw new IllegalArgumentException(
                    "Identificativi non validi.");
        }

        Long companyId =
                companyContext.requireCurrentCompanyId();

        CreditNote note = notes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Nota di credito non trovata."));

        if (!"SUPPLIER".equals(note.getCreditNoteType())) {
            throw new IllegalArgumentException(
                    "Nota di credito non valida.");
        }

        if (!"REGISTERED".equals(note.getStatus())) {
            return new CompensationView(
                    null, List.of(), false);
        }

        CreditNoteFinancialPosition position = positions
                .findByTenantIdAndCompanyIdAndCreditNoteId(
                        tenantId, companyId, creditNoteId)
                .orElse(null);

        if (position == null
                || !"SUPPLIER".equals(position.getPositionType())
                || !List.of("OPEN", "PARTIALLY_SETTLED")
                .contains(position.getStatus())
                || position.getAvailableAmount() == null
                || position.getAvailableAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            return new CompensationView(
                    position, List.of(), false);
        }

        List<PaymentDue> eligibleDues =
                dues.findCompensableSupplierDues(
                        tenantId,
                        companyId,
                        note.getPartyId(),
                        note.getCurrencyCode()
                );

        return new CompensationView(
                position,
                List.copyOf(eligibleDues),
                !eligibleDues.isEmpty()
        );
    }
}
