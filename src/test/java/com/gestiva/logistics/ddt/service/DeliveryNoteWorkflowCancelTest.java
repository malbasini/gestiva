package com.gestiva.logistics.ddt.service;

import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.service.InventoryDocumentPostingService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryNoteWorkflowCancelTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long DDT_ID = 77L;

    @Mock CompanyContext companyContext;
    @Mock DeliveryNoteRepository deliveryNoteRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock InventoryDocumentPostingService inventoryDocumentPostingService;

    @InjectMocks DeliveryNoteWorkflowService service;

    private DeliveryNote prepareDdt(String status) {
        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        DeliveryNote ddt = new DeliveryNote();
        ddt.setTenantId(TENANT_ID);
        ddt.setCompanyId(COMPANY_ID);
        ddt.setDdtNumber("DDT-TEST-001");
        ddt.setStatus(status);

        when(deliveryNoteRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, DDT_ID))
                .thenReturn(Optional.of(ddt));

        return ddt;
    }

    private void verifyNoWrites() {
        verify(deliveryNoteRepository, never()).save(any());
        verify(deliveryNoteRepository, never())
                .delete(any(DeliveryNote.class));
        verifyNoInteractions(inventoryDocumentPostingService);
    }

    @Test
    void shouldRejectWithoutActiveCompany() {
        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(null);

        assertThrows(BusinessException.class,
                () -> service.cancel(TENANT_ID, DDT_ID));

        verifyNoInteractions(
                deliveryNoteRepository,
                invoiceRepository,
                inventoryDocumentPostingService);
    }

    @Test
    void shouldRejectDdtFromOtherCompany() {
        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(deliveryNoteRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, DDT_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.cancel(TENANT_ID, DDT_ID));

        verifyNoWrites();
        verifyNoInteractions(invoiceRepository);
    }

    @Test
    void shouldRejectAlreadyCancelledDdt() {
        DeliveryNote ddt = prepareDdt("CANCELLED");

        assertThrows(BusinessException.class,
                () -> service.cancel(TENANT_ID, DDT_ID));

        assertEquals("CANCELLED", ddt.getStatus());
        verifyNoWrites();
        verifyNoInteractions(invoiceRepository);
    }

    @Test
    void shouldRejectNonIssuedDdt() {
        DeliveryNote ddt = prepareDdt("DRAFT");

        assertThrows(BusinessException.class,
                () -> service.cancel(TENANT_ID, DDT_ID));

        assertEquals("DRAFT", ddt.getStatus());
        verifyNoWrites();
        verifyNoInteractions(invoiceRepository);
    }

    @Test
    void shouldRejectInvoicedDdtWithoutReversingStock() {
        DeliveryNote ddt = prepareDdt("ISSUED");

        when(invoiceRepository
                .existsByTenantIdAndCompanyIdAndDeliveryNoteId(
                        TENANT_ID, COMPANY_ID, DDT_ID))
                .thenReturn(true);

        assertThrows(BusinessException.class,
                () -> service.cancel(TENANT_ID, DDT_ID));

        assertEquals("ISSUED", ddt.getStatus());
        verifyNoWrites();
    }

    @Test
    void shouldReverseStockBeforeCancellingIssuedDdt() {
        DeliveryNote ddt = prepareDdt("ISSUED");

        // Nessuna fattura associata: Mockito restituisce
        // false per il metodo booleano del repository.

        service.cancel(TENANT_ID, DDT_ID);

        assertEquals("CANCELLED", ddt.getStatus());

        InOrder order = inOrder(
                invoiceRepository,
                inventoryDocumentPostingService,
                deliveryNoteRepository);

        order.verify(invoiceRepository)
                .existsByTenantIdAndCompanyIdAndDeliveryNoteId(
                        TENANT_ID, COMPANY_ID, DDT_ID);

        order.verify(inventoryDocumentPostingService)
                .reverseDocumentMovementsV2(
                        eq(TENANT_ID),
                        eq(COMPANY_ID),
                        eq("DELIVERY_NOTE"),
                        eq(DDT_ID),
                        any(LocalDate.class),
                        eq("Annullamento DDT DDT-TEST-001"));

        order.verify(deliveryNoteRepository).save(same(ddt));

        verify(deliveryNoteRepository, never())
                .delete(any(DeliveryNote.class));
    }

    @Test
    void shouldKeepDdtIssuedWhenInventoryReversalFails() {

        DeliveryNote ddt = prepareDdt("ISSUED");

        BusinessException inventoryError = new BusinessException(
                "Storno FIFO non riuscito");

        doThrow(inventoryError)
                .when(inventoryDocumentPostingService)
                .reverseDocumentMovementsV2(
                        eq(TENANT_ID),
                        eq(COMPANY_ID),
                        eq("DELIVERY_NOTE"),
                        eq(DDT_ID),
                        any(LocalDate.class),
                        eq("Annullamento DDT DDT-TEST-001"));

        BusinessException thrown = assertThrows(
                BusinessException.class,
                () -> service.cancel(TENANT_ID, DDT_ID));

        assertSame(inventoryError, thrown);

        // Il documento deve mantenere il suo stato originale.
        assertEquals("ISSUED", ddt.getStatus());

        // Lo storno è stato tentato.
        verify(inventoryDocumentPostingService)
                .reverseDocumentMovementsV2(
                        eq(TENANT_ID),
                        eq(COMPANY_ID),
                        eq("DELIVERY_NOTE"),
                        eq(DDT_ID),
                        any(LocalDate.class),
                        eq("Annullamento DDT DDT-TEST-001"));

        // Nessun salvataggio del DDT come CANCELLED.
        verify(deliveryNoteRepository, never()).save(any());

        // Nessuna cancellazione fisica.
        verify(deliveryNoteRepository, never())
                .delete(any(DeliveryNote.class));

        verify(invoiceRepository)
                .existsByTenantIdAndCompanyIdAndDeliveryNoteId(
                        TENANT_ID, COMPANY_ID, DDT_ID);
    }

}
