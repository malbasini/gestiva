package com.gestiva.logistics.ddt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.movement.service.InventoryDocumentPostingService;
import com.gestiva.warehouse.inventory.movement.service.InventoryMovementService;
import com.gestiva.logistics.ddt.dto.DeliveryNoteResponse;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.entity.DeliveryNoteLine;
import com.gestiva.logistics.ddt.repository.DeliveryNoteLineRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.entity.SalesOrderLine;
import com.gestiva.sales.order.repository.SalesOrderLineRepository;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.service.InventoryLocationValidationService;

@Service
@Transactional
public class DeliveryNoteService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final DeliveryNoteLineRepository deliveryNoteLineRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final InventoryDocumentPostingService inventoryDocumentPostingService;
    private final DocumentSequenceService documentSequenceService;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementService inventoryMovementService;
    private final CompanyContext companyContext;
    private final InventoryLocationValidationService inventoryLocationValidationService;


    public DeliveryNoteService(
            DeliveryNoteRepository deliveryNoteRepository,
            DeliveryNoteLineRepository deliveryNoteLineRepository,
            SalesOrderRepository salesOrderRepository,
            SalesOrderLineRepository salesOrderLineRepository,
            InventoryDocumentPostingService inventoryDocumentPostingService,
            DocumentSequenceService documentSequenceService,
            InventoryMovementRepository inventoryMovementRepository,
            InventoryMovementService inventoryMovementService,
            CompanyContext companyContext,
            InventoryLocationValidationService inventoryLocationValidationService){

        this.deliveryNoteRepository = deliveryNoteRepository;
        this.deliveryNoteLineRepository = deliveryNoteLineRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.inventoryDocumentPostingService = inventoryDocumentPostingService;
        this.documentSequenceService = documentSequenceService;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryMovementService = inventoryMovementService;
        this.companyContext = companyContext;
        this.inventoryLocationValidationService = inventoryLocationValidationService;
    }

    private Long requireCompanyId() {
        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata."
            );
        }
        return companyId;
    }

    public DeliveryNoteResponse createFromSalesOrder(
            Long tenantId,
            Long salesOrderId,
            Long warehouseId,
            Long locationId) {

        Long companyId = requireCompanyId();

        inventoryLocationValidationService.validate(
                tenantId,
                companyId,
                warehouseId,
                locationId
        );

        SalesOrder order = salesOrderRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        salesOrderId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Ordine non trovato nella società attiva"
                ));

        validateOrderCanGenerateDeliveryNote(
                order,
                tenantId,
                companyId,
                salesOrderId
        );

        List<SalesOrderLine> orderLines = salesOrderLineRepository
                .findByTenantIdAndCompanyIdAndSalesOrderIdOrderByLineNoAsc(
                        tenantId,
                        companyId,
                        salesOrderId
                );

        if (orderLines.isEmpty()) {
            throw new BusinessException("Impossibile creare un DDT da un ordine senza righe.");
        }

        OffsetDateTime now = OffsetDateTime.now();
        DeliveryNote note = new DeliveryNote();
        note.setTenantId(tenantId);
        note.setCompanyId(companyId);
        note.setWarehouseId(warehouseId);
        note.setLocationId(locationId);
        note.setSalesOrderId(order.getId());
        note.setCustomerId(order.getCustomerId());
        note.setDdtNumber(generateNextDdtNumber(tenantId, companyId));
        note.setDdtDate(LocalDate.now());
        note.setStatus("ISSUED");

        note.setTransportReason("Vendita");
        note.setCarriageCondition("Franco");
        note.setCarrierName(null);

        note.setCurrencyCode(order.getCurrencyCode());
        note.setSubtotalAmount(defaultZero(order.getSubtotalAmount()));
        note.setTaxAmount(defaultZero(order.getTaxAmount()));
        note.setTotalAmount(defaultZero(order.getTotalAmount()));
        note.setNotes(null);
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        DeliveryNote savedNote = deliveryNoteRepository.save(note);
        DeliveryNoteLine line = null;
        int lineNo = 1;
        for (SalesOrderLine orderLine : orderLines) {
            line = new DeliveryNoteLine();
            line.setTenantId(tenantId);
            line.setCompanyId(companyId);
            line.setDeliveryNoteId(savedNote.getId());
            line.setSalesOrderLineId(orderLine.getId());
            line.setLineNo(lineNo++);
            line.setDescription(orderLine.getDescription());
            line.setQuantity(orderLine.getQuantity());
            line.setUnitOfMeasure(orderLine.getUnitOfMeasure());
            line.setUnitPrice(orderLine.getUnitPrice());
            line.setDiscountPct(defaultZero(orderLine.getDiscountPct()));
            line.setTaxPct(defaultZero(orderLine.getTaxPct()));
            line.setTaxAmount(defaultZero(orderLine.getTaxAmount()));
            line.setLineTotal(defaultZero(orderLine.getLineTotal()));
            line.setItemId(orderLine.getItemId());
            deliveryNoteLineRepository.save(line);
        }
        inventoryDocumentPostingService.postSalesDeliveryFromDeliveryNoteV2(
                tenantId,
                companyId,
                savedNote.getId()
        );
        return toResponse(savedNote);
    }

    private void validateOrderCanGenerateDeliveryNote(
            SalesOrder order,
            Long tenantId,
            Long companyId,
            Long salesOrderId) {

        if (!"FULFILLED".equals(order.getStatus())) {
            throw new BusinessException(
                    "Il DDT può essere creato solo da un ordine FULFILLED."
            );
        }

        boolean alreadyExists = deliveryNoteRepository
                .existsByTenantIdAndCompanyIdAndSalesOrderId(
                        tenantId,
                        companyId,
                        salesOrderId
                );

        if (alreadyExists) {
            throw new BusinessException(
                    "Esiste già un DDT associato a questo ordine nella società attiva."
            );
        }
    }
    private String generateNextDdtNumber(Long tenantId, Long companyId) {
        return documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.DELIVERY_NOTE
        );
    }
    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private DeliveryNoteResponse toResponse(DeliveryNote note) {
        DeliveryNoteResponse response = new DeliveryNoteResponse();
        response.setId(note.getId());
        response.setCustomerId(note.getCustomerId());
        response.setSalesOrderId(note.getSalesOrderId());
        response.setDdtNumber(note.getDdtNumber());
        response.setDdtDate(note.getDdtDate());
        response.setStatus(note.getStatus());
        response.setCurrencyCode(note.getCurrencyCode());
        response.setSubtotalAmount(note.getSubtotalAmount());
        response.setTaxAmount(note.getTaxAmount());
        response.setTotalAmount(note.getTotalAmount());
        return response;
    }
}