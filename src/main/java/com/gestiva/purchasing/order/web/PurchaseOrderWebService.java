package com.gestiva.purchasing.order.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.common.util.NumberInputUtils;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.item.repository.ItemUomRepository;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.security.company.context.CompanyContext;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PurchaseOrderWebService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private final ItemUomRepository itemUomRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final DocumentSequenceService documentSequenceService;
    private final CompanyContext companyContext;



    public PurchaseOrderWebService(PurchaseOrderRepository purchaseOrderRepository,
                                   PurchaseOrderLineRepository purchaseOrderLineRepository,
                                   SupplierRepository supplierRepository,
                                   ItemRepository itemRepository,
                                   ItemUomRepository itemUomRepository,
                                   GoodsReceiptRepository goodsReceiptRepository,
                                   GoodsReceiptLineRepository goodsReceiptLineRepository,
                                   DocumentSequenceService documentSequenceService,
                                   CompanyContext companyContext) {

        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.supplierRepository = supplierRepository;
        this.itemRepository = itemRepository;
        this.itemUomRepository = itemUomRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.documentSequenceService = documentSequenceService;
        this.companyContext = companyContext;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderForm buildCreateForm() {
        PurchaseOrderForm form = new PurchaseOrderForm();
        form.setOrderDate(LocalDate.now());
        form.setExpectedDeliveryDate(LocalDate.now().plusDays(7));
        form.setStatus("DRAFT");
        form.setCurrencyCode("EUR");
        form.getLines().add(defaultLine());
        return form;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderListItemView> findAll(Long tenantId) {
        Long companyId = companyContext.resolveCurrentCompanyId();

        var orders = purchaseOrderRepository.findAll(
                org.springframework.data.jpa.domain.Specification
                        .where(byTenantAndCompany(tenantId, companyId)),
                Sort.by(Sort.Direction.DESC, "orderDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        List<PurchaseOrderListItemView> result = new ArrayList<>();
        for (var po : orders) {
            PurchaseOrderListItemView v = new PurchaseOrderListItemView();
            v.setId(po.getId());
            v.setOrderNumber(po.getOrderNumber());
            v.setFormattedOrderDate(PdfFormatUtils.formatDate(po.getOrderDate()));
            v.setSupplierName(
                    supplierRepository.findByTenantIdAndId(tenantId, po.getSupplierId())
                            .map(s -> s.getName())
                            .orElse("-")
            );
            v.setStatus(po.getStatus());
            v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(po.getTotalAmount()));
            result.add(v);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDetailView getDetail(Long tenantId, Long id) {
        Long companyId = companyContext.resolveCurrentCompanyId();

        var po = purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        id
                )
                .orElseThrow(() -> new NotFoundException("Ordine fornitore non trovato"));

        var lines = purchaseOrderLineRepository.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(tenantId, id);

        PurchaseOrderDetailView v = new PurchaseOrderDetailView();
        v.setId(po.getId());
        v.setOrderNumber(po.getOrderNumber());
        v.setFormattedOrderDate(PdfFormatUtils.formatDate(po.getOrderDate()));
        v.setFormattedExpectedDeliveryDate(po.getExpectedDeliveryDate() != null ? PdfFormatUtils.formatDate(po.getExpectedDeliveryDate()) : "-");
        v.setSupplierName(
                supplierRepository.findByTenantIdAndId(tenantId, po.getSupplierId())
                        .map(s -> s.getName())
                        .orElse("-")
        );
        v.setStatus(po.getStatus());
        v.setCurrencyCode(po.getCurrencyCode());
        v.setNotes(po.getNotes());
        v.setFormattedSubtotalAmount(PdfFormatUtils.formatMoney(po.getSubtotalAmount()));
        v.setFormattedTaxAmount(PdfFormatUtils.formatMoney(po.getTaxAmount()));
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(po.getTotalAmount()));
        var receiptOpt = goodsReceiptRepository.findFirstByTenantIdAndCompanyIdAndPurchaseOrderId(tenantId, companyId, po.getId());
        v.setHasGoodsReceipt(receiptOpt.isPresent());
        v.setCanReceiveGoods("CONFIRMED".equalsIgnoreCase(po.getStatus()) && receiptOpt.isEmpty());
        v.setCanEdit(!"CONFIRMED".equalsIgnoreCase(po.getStatus()) && !"CANCELLED".equalsIgnoreCase(po.getStatus()));
        receiptOpt.ifPresent(receipt -> {
            v.setGoodsReceiptId(receipt.getId());
            v.setGoodsReceiptNumber(receipt.getReceiptNumber());
        });
        for (var line : lines) {
            PurchaseOrderDetailLineView lv = new PurchaseOrderDetailLineView();
            lv.setLineNo(line.getLineNo());
            lv.setDescription(line.getDescription());
            lv.setFormattedQuantity(PdfFormatUtils.formatDecimalTrimmed(line.getQuantity(),3));
            lv.setFormattedUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            lv.setFormattedDiscountPct(PdfFormatUtils.formatDecimal(line.getDiscountPct()) + "%");
            lv.setFormattedTaxPct(PdfFormatUtils.formatDecimalTrimmed(line.getTaxPct(),2) + "%");
            lv.setFormattedLineTotal(PdfFormatUtils.formatMoney(line.getLineTotal()));
            v.getLines().add(lv);
        }

        return v;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderForm getForm(Long tenantId, Long id) {
        Long companyId = companyContext.resolveCurrentCompanyId();

        var po = purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        id
                )
                .orElseThrow(() -> new NotFoundException("Ordine fornitore non trovato"));

        var lines = purchaseOrderLineRepository.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(tenantId, id);

        PurchaseOrderForm form = new PurchaseOrderForm();
        form.setSupplierId(po.getSupplierId());
        form.setOrderDate(po.getOrderDate());
        form.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        form.setStatus(po.getStatus());
        form.setCurrencyCode(po.getCurrencyCode());
        form.setNotes(po.getNotes());

        for (var line : lines) {
            PurchaseOrderLineForm lf = new PurchaseOrderLineForm();

            // Identificativo della riga originale.
            lf.setLineId(line.getId());

            // Unità commerciale e fattore storico.
            // Nessun ricalcolo dall'anagrafica articolo.
            lf.setUomId(line.getUomId());
            lf.setConversionFactor(
                    line.getConversionFactor() != null
                            ? line.getConversionFactor().toPlainString()
                            : null
            );

            lf.setItemId(line.getItemId());
            lf.setDescription(line.getDescription());
            lf.setQuantity(PdfFormatUtils.formatDecimalTrimmed(line.getQuantity(),3));
            lf.setUnitPrice(PdfFormatUtils.formatMoney(line.getUnitPrice()));
            lf.setDiscountPct(PdfFormatUtils.formatDecimalTrimmed(line.getDiscountPct(),2));
            lf.setTaxPct(PdfFormatUtils.formatDecimalTrimmed(line.getTaxPct(),2));
            form.getLines().add(lf);
        }

        if (form.getLines().isEmpty()) {
            form.getLines().add(defaultLine());
        }

        return form;
    }

    public PurchaseOrderTotalsPreviewView calculatePreviewTotals(
            PurchaseOrderForm form
    ) {
        if (form == null) {
            throw new BusinessException("Ordine fornitore non valido");
        }

        validateLines(form.getLines());

        Totals totals = calculateTotals(form.getLines());

        PurchaseOrderTotalsPreviewView view =
                new PurchaseOrderTotalsPreviewView();

        view.setFormattedSubtotalAmount(
                PdfFormatUtils.formatMoney(totals.subtotal())
        );
        view.setFormattedTaxAmount(
                PdfFormatUtils.formatMoney(totals.tax())
        );
        view.setFormattedTotalAmount(
                PdfFormatUtils.formatMoney(totals.total())
        );

        return view;
    }

    public void validateLines(List<PurchaseOrderLineForm> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("L'ordine deve contenere almeno una riga");
        }

        for (int i = 0; i < lines.size(); i++) {
            PurchaseOrderLineForm line = lines.get(i);
            int lineNo = i + 1;

            if (line == null) {
                throw new BusinessException("La riga " + lineNo + " non è valida");
            }

            if (line.getDescription() == null || line.getDescription().isBlank()) {
                throw new BusinessException(
                    "La descrizione della riga " + lineNo + " è obbligatoria"
                );
            }

            if (line.getQuantity() == null || line.getQuantity().isBlank()) {
                throw new BusinessException(
                    "La quantità della riga " + lineNo + " è obbligatoria"
                );
            }

            if (line.getUnitPrice() == null || line.getUnitPrice().isBlank()) {
                throw new BusinessException(
                    "Il prezzo unitario della riga " + lineNo + " è obbligatorio"
                );
            }

            if (line.getDiscountPct() == null || line.getDiscountPct().isBlank()) {
                throw new BusinessException(
                    "Lo sconto della riga " + lineNo + " è obbligatorio"
                );
            }

            if (line.getTaxPct() == null || line.getTaxPct().isBlank()) {
                throw new BusinessException(
                    "L'IVA della riga " + lineNo + " è obbligatoria"
                );
            }

            BigDecimal quantity = parseOrderQuantity(
                line.getQuantity()
            );

            BigDecimal unitPrice = NumberInputUtils.parseDecimal(
                line.getUnitPrice(), "prezzo unitario"
            );

            BigDecimal discountPct = NumberInputUtils.parseDecimal(
                line.getDiscountPct(), "sconto"
            );

            BigDecimal taxPct = NumberInputUtils.parseDecimal(
                line.getTaxPct(), "IVA"
            );

            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(
                    "La quantità della riga " + lineNo + " deve essere maggiore di zero"
                );
            }

            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(
                    "Il prezzo unitario della riga " + lineNo + " non può essere negativo"
                );
            }

            if (unitPrice.stripTrailingZeros().scale() > 2) {
                throw new BusinessException(
                    "Il prezzo unitario della riga " + lineNo +
                    " può avere al massimo 2 decimali"
                );
            }

            // La colonna unit_price è DECIMAL(15,2):
            // massimo 13 cifre intere, senza troncamenti.
            BigDecimal normalizedUnitPrice = unitPrice.setScale(
                    2,
                    RoundingMode.UNNECESSARY
            );

            if (normalizedUnitPrice.precision()
                    - normalizedUnitPrice.scale() > 13) {
                throw new BusinessException(
                    "Il prezzo unitario della riga " + lineNo
                    + " supera la capacità massima consentita"
                );
            }

            if (discountPct.compareTo(BigDecimal.ZERO) < 0 ||
                discountPct.compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException(
                    "Lo sconto della riga " + lineNo + " deve essere tra 0 e 100"
                );
            }

            if (discountPct.stripTrailingZeros().scale() > 2) {
                throw new BusinessException(
                    "Lo sconto della riga " + lineNo +
                    " può avere al massimo 2 decimali"
                );
            }

            if (taxPct.compareTo(BigDecimal.ZERO) < 0 ||
                taxPct.compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException(
                    "L'IVA della riga " + lineNo + " deve essere tra 0 e 100"
                );
            }

            if (taxPct.stripTrailingZeros().scale() > 2) {
                throw new BusinessException(
                    "L'IVA della riga " + lineNo +
                    " può avere al massimo 2 decimali"
                );
            }
        }
    }

    public Long create(Long tenantId, PurchaseOrderForm form) {
        Long companyId = companyContext.resolveCurrentCompanyId();

        validateLines(form.getLines());
        validateNewLineIds(form.getLines());
        validateHeader(tenantId, companyId, form);

        String orderNumber = nextOrderNumber(tenantId, companyId);

        PurchaseOrder po = new PurchaseOrder();
        po.setTenantId(tenantId);
        po.setCompanyId(companyId);
        po.setOrderNumber(orderNumber);
        applyHeader(po, form);

        Totals totals = calculateTotals(form.getLines());
        po.setSubtotalAmount(totals.subtotal());
        po.setTaxAmount(totals.tax());
        po.setTotalAmount(totals.total());

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        saveLines(tenantId, saved.getId(), form.getLines());

        return saved.getId();
    }

    public void update(Long tenantId, Long id, PurchaseOrderForm form) {
        Long companyId = companyContext.resolveCurrentCompanyId();

        var po = purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        id
                )
                .orElseThrow(() -> new NotFoundException("Ordine fornitore non trovato"));

        if ("CONFIRMED".equalsIgnoreCase(po.getStatus()) || "CANCELLED".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException("Questo ordine fornitore non è più modificabile.");
        }

        validateLines(form.getLines());

        // Verifiche preventive sugli identificativi persistenti.
        validateExistingLineIds(tenantId, id, form.getLines());

        // Protezione delle righe già collegate a ricevimenti merci.
        validateRemovedLines(tenantId, id, form.getLines());
        validateReceivedLinesUnchanged(tenantId, id, form.getLines());

        validateHeader(tenantId, po.getCompanyId(), form);
        applyHeader(po, form);

        Totals totals = calculateTotals(form.getLines());
        po.setSubtotalAmount(totals.subtotal());
        po.setTaxAmount(totals.tax());
        po.setTotalAmount(totals.total());

        purchaseOrderRepository.save(po);

        // Elimina soltanto le righe rimosse dal form.
        // Le righe collegate a ricevimenti merci sono protette.
        deleteRemovedLines(tenantId, id, form.getLines());

        // Aggiorna le righe esistenti mantenendo gli ID.
        // Inserisce le righe nuove con nuovi identificativi.
        saveLines(tenantId, id, form.getLines());
    }

    private void validateHeader(Long tenantId, Long companyId, PurchaseOrderForm form) {
        if (form.getSupplierId() == null) {
            throw new BusinessException("Il fornitore è obbligatorio.");
        }

        supplierRepository.findByTenantIdAndId(tenantId, form.getSupplierId())
                .orElseThrow(() -> new BusinessException(
                        "Fornitore non valido per il tenant corrente."
                ));

        if (form.getLines() == null || form.getLines().isEmpty()) {
            throw new BusinessException("Inserire almeno una riga.");
        }
    }

    private void applyHeader(PurchaseOrder po, PurchaseOrderForm form) {
        po.setSupplierId(form.getSupplierId());
        po.setOrderDate(form.getOrderDate());
        po.setExpectedDeliveryDate(form.getExpectedDeliveryDate());
        po.setStatus(form.getStatus());
        po.setCurrencyCode(form.getCurrencyCode());
        po.setNotes(form.getNotes());
    }


    /**
     * Verifica che ogni identificativo di riga ricevuto dal form
     * appartenga all'ordine corrente e non sia duplicato.
     *
     * Le righe nuove hanno lineId null.
     */
    /**
     * In fase di creazione tutte le righe devono essere nuove.
     * Un identificativo persistente non può provenire dal browser.
     */
    private void validateNewLineIds(
            List<PurchaseOrderLineForm> lines
    ) {
        for (PurchaseOrderLineForm line : lines) {
            if (line.getLineId() != null) {
                throw new BusinessException(
                        "Una nuova riga ordine non può avere "
                        + "un identificativo preesistente."
                );
            }
        }
    }

    private void validateExistingLineIds(
            Long tenantId,
            Long purchaseOrderId,
            List<PurchaseOrderLineForm> lines
    ) {
        java.util.Set<Long> seenIds = new java.util.HashSet<>();

        for (PurchaseOrderLineForm lineForm : lines) {
            Long lineId = lineForm.getLineId();

            if (lineId == null) {
                continue;
            }

            if (!seenIds.add(lineId)) {
                throw new BusinessException(
                        "La stessa riga dell'ordine è presente più volte."
                );
            }

            purchaseOrderLineRepository
                    .findByTenantIdAndPurchaseOrderIdAndId(
                            tenantId,
                            purchaseOrderId,
                            lineId
                    )
                    .orElseThrow(() -> new BusinessException(
                            "Riga ordine non valida per il documento corrente."
                    ));
        }
    }


    /**
     * Impedisce la rimozione di righe di ordine già referenziate
     * da un ricevimento merci.
     *
     * Il metodo non modifica alcun documento.
     */
    private void validateRemovedLines(
            Long tenantId,
            Long purchaseOrderId,
            List<PurchaseOrderLineForm> submittedLines
    ) {
        java.util.Set<Long> submittedIds = new java.util.HashSet<>();

        for (PurchaseOrderLineForm submitted : submittedLines) {
            if (submitted.getLineId() != null) {
                submittedIds.add(submitted.getLineId());
            }
        }

        List<PurchaseOrderLine> originalLines =
                purchaseOrderLineRepository
                        .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                                tenantId,
                                purchaseOrderId
                        );

        for (PurchaseOrderLine originalLine : originalLines) {
            if (submittedIds.contains(originalLine.getId())) {
                continue;
            }

            if (goodsReceiptLineRepository.existsByTenantIdAndPurchaseOrderLineId(
                    tenantId,
                    originalLine.getId()
            )) {
                throw new BusinessException(
                        "Impossibile eliminare una riga già collegata "
                        + "a un ricevimento merci."
                );
            }
        }
    }


    /**
     * Protegge le righe già collegate a ricevimenti merci.
     *
     * Una riga ricevuta può essere ripresentata dal form
     * soltanto se tutti i suoi dati documentali sono invariati.
     *
     * Il fattore storico viene confrontato con quello persistito:
     * il valore inviato dal browser non è una fonte attendibile
     * per i successivi salvataggi.
     */
    private void validateReceivedLinesUnchanged(
            Long tenantId,
            Long purchaseOrderId,
            List<PurchaseOrderLineForm> submittedLines
    ) {
        for (PurchaseOrderLineForm submitted : submittedLines) {
            Long lineId = submitted.getLineId();

            if (lineId == null) {
                continue;
            }

            PurchaseOrderLine original =
                    purchaseOrderLineRepository
                            .findByTenantIdAndPurchaseOrderIdAndId(
                                    tenantId,
                                    purchaseOrderId,
                                    lineId
                            )
                            .orElseThrow(() -> new BusinessException(
                                    "Riga ordine non valida."
                            ));

            if (!goodsReceiptLineRepository
                    .existsByTenantIdAndPurchaseOrderLineId(
                            tenantId,
                            lineId
                    )) {
                continue;
            }

            boolean unchanged =
                    java.util.Objects.equals(
                            original.getItemId(),
                            submitted.getItemId()
                    )
                    && java.util.Objects.equals(
                            original.getDescription(),
                            submitted.getDescription() != null
                                    ? submitted.getDescription().trim()
                                    : null
                    )
                    && original.getQuantity().compareTo(
                            parseOrderQuantity(submitted.getQuantity())
                    ) == 0
                    && original.getUnitPrice().compareTo(
                            NumberInputUtils.parseDecimal(
                                    submitted.getUnitPrice(),
                                    "prezzo unitario"
                            )
                    ) == 0
                    && original.getDiscountPct().compareTo(
                            NumberInputUtils.parseDecimal(
                                    submitted.getDiscountPct(),
                                    "sconto"
                            )
                    ) == 0
                    && original.getTaxPct().compareTo(
                            NumberInputUtils.parseDecimal(
                                    submitted.getTaxPct(),
                                    "IVA"
                            )
                    ) == 0
                    ;

            if (!unchanged) {
                throw new BusinessException(
                        "Impossibile modificare una riga già "
                        + "collegata a un ricevimento merci."
                );
            }

            // Il fattore di conversione storico è immutabile.
            // Non viene confrontato con il valore del browser,
            // perché dovrà essere preservato dal database
            // durante il successivo aggiornamento della riga.
        }
    }

    /**
     * Salva le righe preservando gli identificativi esistenti.
     *
     * Per le righe già persistite, uomId e conversionFactor
     * rimangono quelli registrati nel database.
     *
     * Il fattore ricevuto dal browser non viene utilizzato.
     */
    /**
     * Elimina soltanto le righe non più presenti nel form.
     *
     * Deve essere chiamato dopo l'acquisizione del lock
     * sull'ordine e dopo le verifiche preventive.
     *
     * Non elimina mai righe collegate a ricevimenti merci.
     */
    private void deleteRemovedLines(
            Long tenantId,
            Long purchaseOrderId,
            List<PurchaseOrderLineForm> submittedLines
    ) {
        java.util.Set<Long> retainedIds =
                new java.util.HashSet<>();

        for (PurchaseOrderLineForm submitted : submittedLines) {
            if (submitted.getLineId() != null) {
                retainedIds.add(submitted.getLineId());
            }
        }

        List<PurchaseOrderLine> originalLines =
                purchaseOrderLineRepository
                        .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                                tenantId,
                                purchaseOrderId
                        );

        for (PurchaseOrderLine original : originalLines) {
            Long originalId = original.getId();

            if (retainedIds.contains(originalId)) {
                continue;
            }

            if (goodsReceiptLineRepository
                    .existsByTenantIdAndPurchaseOrderLineId(
                            tenantId,
                            originalId
                    )) {
                throw new BusinessException(
                        "Impossibile eliminare una riga "
                        + "collegata a un ricevimento merci."
                );
            }

            long deleted =
                    purchaseOrderLineRepository
                            .deleteByTenantIdAndPurchaseOrderIdAndId(
                                    tenantId,
                                    purchaseOrderId,
                                    originalId
                            );

            if (deleted != 1L) {
                throw new BusinessException(
                        "Eliminazione riga ordine non riuscita "
                        + "in modo univoco."
                );
            }
        }
    }

    /**
     * Risolve il fattore di conversione per un'unità di acquisto.
     *
     * Il fattore proviene esclusivamente dal database.
     * Non utilizzare conversionFactor ricevuto dal browser.
     *
     * Metodo predisposto ma non ancora collegato al salvataggio.
     */
    private BigDecimal resolvePurchaseConversionFactor(
            Long tenantId,
            Long itemId,
            Long uomId
    ) {
        if (itemId == null || uomId == null) {
            throw new BusinessException(
                    "Articolo e unità di misura sono obbligatori."
            );
        }

        var item = itemRepository
                .findWithBaseUomByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new BusinessException(
                        "Articolo non trovato."
                ));

        if (!item.isActive()) {
            throw new BusinessException(
                    "Non è possibile acquistare un articolo inattivo."
            );
        }

        var baseUom = item.getBaseUom();

        if (baseUom == null || !baseUom.isActive()) {
            throw new BusinessException(
                    "Unità di misura base non disponibile."
            );
        }

        if (baseUom.getId().equals(uomId)) {
            return BigDecimal.ONE.setScale(6);
        }

        var alternative = itemUomRepository
                .findByTenantIdAndItemIdAndUomId(
                        tenantId,
                        itemId,
                        uomId
                )
                .orElseThrow(() -> new BusinessException(
                        "Unità alternativa non configurata per l'articolo."
                ));

        if (!alternative.isActive()) {
            throw new BusinessException(
                    "Unità alternativa non attiva."
            );
        }

        if (!alternative.isPurchaseUom()) {
            throw new BusinessException(
                    "Unità alternativa non autorizzata per gli acquisti."
            );
        }

        var alternativeUom = alternative.getUom();

        if (alternativeUom == null || !alternativeUom.isActive()) {
            throw new BusinessException(
                    "Unità alternativa non disponibile."
            );
        }

        if (baseUom.getDimension() != alternativeUom.getDimension()) {
            throw new BusinessException(
                    "Unità di misura con dimensione incompatibile."
            );
        }

        BigDecimal factor = alternative.getConversionFactor();

        if (factor == null
                || factor.compareTo(BigDecimal.ZERO) <= 0
                || factor.stripTrailingZeros().scale() > 6
                || factor.precision() - factor.scale() > 13) {
            throw new BusinessException(
                    "Fattore di conversione non valido."
            );
        }

        return factor.setScale(6, RoundingMode.UNNECESSARY);
    }

    /**
     * Verifica che una quantità commerciale sia convertibile
     * esattamente nella quantità base utilizzata dal magazzino.
     *
     * Non modifica l'ordine e non registra movimenti.
     *
     * Metodo predisposto ma non ancora collegato al salvataggio.
     */
    private BigDecimal validatePurchaseUomQuantity(
            Long tenantId,
            Long itemId,
            Long uomId,
            BigDecimal commercialQuantity,
            BigDecimal conversionFactor
    ) {
        if (commercialQuantity == null
                || commercialQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità di acquisto deve essere positiva."
            );
        }

        if (conversionFactor == null
                || conversionFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Fattore di conversione non valido."
            );
        }

        // La quantità commerciale deve essere rappresentabile
        // esattamente nella colonna DECIMAL(15,3).
        if (commercialQuantity.stripTrailingZeros().scale() > 3) {
            throw new BusinessException(
                    "La quantità commerciale può avere "
                    + "al massimo 3 decimali."
            );
        }

        BigDecimal normalizedCommercialQuantity =
                commercialQuantity.setScale(
                        3,
                        RoundingMode.UNNECESSARY
                );

        if (normalizedCommercialQuantity.precision()
                - normalizedCommercialQuantity.scale() > 12) {
            throw new BusinessException(
                    "La quantità commerciale supera "
                    + "la capacità massima consentita."
            );
        }

        var item = itemRepository
                .findWithBaseUomByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new BusinessException(
                        "Articolo non trovato."
                ));

        var baseUom = item.getBaseUom();

        if (baseUom == null) {
            throw new BusinessException(
                    "Unità base dell'articolo non disponibile."
            );
        }

        int commercialDecimalPlaces;

        if (baseUom.getId().equals(uomId)) {
            commercialDecimalPlaces = baseUom.getDecimalPlaces();
        } else {
            var alternative = itemUomRepository
                    .findByTenantIdAndItemIdAndUomId(
                            tenantId,
                            itemId,
                            uomId
                    )
                    .orElseThrow(() -> new BusinessException(
                            "Unità alternativa non configurata."
                    ));

            commercialDecimalPlaces =
                    alternative.getUom().getDecimalPlaces();
        }

        if (commercialQuantity.stripTrailingZeros().scale()
                > commercialDecimalPlaces) {
            throw new BusinessException(
                    "La quantità non rispetta i decimali "
                    + "dell'unità commerciale."
            );
        }

        BigDecimal baseQuantity =
                commercialQuantity.multiply(conversionFactor);

        if (baseQuantity.stripTrailingZeros().scale()
                > baseUom.getDecimalPlaces()) {
            throw new BusinessException(
                    "La quantità convertita non rispetta "
                    + "i decimali dell'unità base."
            );
        }

        if (baseQuantity.stripTrailingZeros().scale() > 3) {
            throw new BusinessException(
                    "La quantità convertita supera "
                    + "la precisione del magazzino."
            );
        }

        BigDecimal normalized;

        try {
            normalized = baseQuantity.setScale(
                    3,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "La quantità convertita non è rappresentabile "
                    + "senza arrotondamenti."
            );
        }

        if (normalized.precision() - normalized.scale() > 12) {
            throw new BusinessException(
                    "La quantità convertita supera "
                    + "la capacità del magazzino."
            );
        }

        return normalized;
    }

    /**
     * Verifica la quantità di una riga con snapshot UoM storico.
     *
     * Il fattore proviene esclusivamente dalla riga persistita.
     * Non viene ricalcolato dalla configurazione corrente.
     *
     * Non modifica documenti e non registra movimenti.
     */
    private BigDecimal validateHistoricalPurchaseUomQuantity(
            Long uomId,
            BigDecimal conversionFactor,
            BigDecimal commercialQuantity
    ) {
        if (uomId == null || conversionFactor == null) {
            throw new BusinessException(
                    "Snapshot dell'unità commerciale incompleto."
            );
        }

        if (conversionFactor.compareTo(BigDecimal.ZERO) <= 0
                || conversionFactor.stripTrailingZeros().scale() > 6
                || conversionFactor.precision()
                - conversionFactor.scale() > 13) {
            throw new BusinessException(
                    "Fattore di conversione storico non valido."
            );
        }

        if (commercialQuantity == null
                || commercialQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità commerciale deve essere positiva."
            );
        }

        if (commercialQuantity.stripTrailingZeros().scale() > 3) {
            throw new BusinessException(
                    "La quantità commerciale può avere "
                    + "al massimo 3 decimali."
            );
        }

        BigDecimal normalizedCommercialQuantity =
                commercialQuantity.setScale(
                        3,
                        RoundingMode.UNNECESSARY
                );

        if (normalizedCommercialQuantity.precision()
                - normalizedCommercialQuantity.scale() > 12) {
            throw new BusinessException(
                    "La quantità commerciale supera "
                    + "la capacità massima consentita."
            );
        }

        BigDecimal baseQuantity =
                normalizedCommercialQuantity.multiply(conversionFactor);

        if (baseQuantity.stripTrailingZeros().scale() > 3) {
            throw new BusinessException(
                    "La quantità base derivata dallo snapshot storico "
                    + "non è rappresentabile senza arrotondamenti."
            );
        }

        BigDecimal normalizedBaseQuantity =
                baseQuantity.setScale(
                        3,
                        RoundingMode.UNNECESSARY
                );

        if (normalizedBaseQuantity.precision()
                - normalizedBaseQuantity.scale() > 12) {
            throw new BusinessException(
                    "La quantità base supera "
                    + "la capacità massima del magazzino."
            );
        }

        return normalizedBaseQuantity;
    }

    private void saveLines(
            Long tenantId,
            Long purchaseOrderId,
            List<PurchaseOrderLineForm> lines
    ) {
        int lineNo = 1;

        for (PurchaseOrderLineForm lineForm : lines) {
            PurchaseOrderLine line;

            if (lineForm.getLineId() == null) {
                // Il fattore inviato dal browser non è attendibile.
                if (lineForm.getConversionFactor() != null
                        && !lineForm.getConversionFactor().isBlank()) {
                    throw new BusinessException(
                            "Il fattore di conversione non può "
                            + "essere fornito dal browser."
                    );
                }

                line = new PurchaseOrderLine();
                line.setTenantId(tenantId);
                line.setPurchaseOrderId(purchaseOrderId);

                if (lineForm.getUomId() != null) {
                    if (lineForm.getItemId() == null) {
                        throw new BusinessException(
                                "L'unità commerciale richiede "
                                + "un articolo selezionato."
                        );
                    }

                    BigDecimal factor =
                            resolvePurchaseConversionFactor(
                                    tenantId,
                                    lineForm.getItemId(),
                                    lineForm.getUomId()
                            );

                    validatePurchaseUomQuantity(
                            tenantId,
                            lineForm.getItemId(),
                            lineForm.getUomId(),
                            parseOrderQuantity(lineForm.getQuantity()),
                            factor
                    );

                    // Snapshot verificato dal backend.
                    line.setUomId(lineForm.getUomId());
                    line.setConversionFactor(factor);
                }
            } else {
                line = purchaseOrderLineRepository
                        .findByTenantIdAndPurchaseOrderIdAndId(
                                tenantId,
                                purchaseOrderId,
                                lineForm.getLineId()
                        )
                        .orElseThrow(() -> new BusinessException(
                                "Riga ordine non valida per il documento corrente."
                        ));

                // Una riga storica senza snapshot non può ricevere
                // una nuova unità commerciale tramite il browser.
                if (line.getUomId() == null
                        && line.getConversionFactor() == null
                        && (lineForm.getUomId() != null
                        || (lineForm.getConversionFactor() != null
                        && !lineForm.getConversionFactor().isBlank()))) {
                    throw new BusinessException(
                            "Impossibile introdurre dati di conversione "
                            + "su una riga storica senza snapshot."
                    );
                }

                // Uno snapshot UoM appartiene all'articolo originale.
                // Non può essere riutilizzato per un articolo diverso.
                if ((line.getUomId() != null
                        || line.getConversionFactor() != null)
                        && !java.util.Objects.equals(
                                line.getItemId(),
                                lineForm.getItemId()
                        )) {
                    throw new BusinessException(
                            "Impossibile cambiare articolo su una riga "
                            + "con unità di misura storica."
                    );
                }

                // Uno snapshot parziale non può essere interpretato.
                if ((line.getUomId() == null)
                        != (line.getConversionFactor() == null)) {
                    throw new BusinessException(
                            "Snapshot UoM storico incompleto: "
                            + "modifica della riga non consentita."
                    );
                }

                // Per uno snapshot completo utilizziamo soltanto
                // il fattore storico persistito, mai quello del browser.
                if (line.getUomId() != null) {
                    validateHistoricalPurchaseUomQuantity(
                            line.getUomId(),
                            line.getConversionFactor(),
                            parseOrderQuantity(lineForm.getQuantity())
                    );
                }
            }

            // Non sovrascrivere uomId o conversionFactor:
            // per le righe esistenti sono dati storici persistiti.
            // Le righe nuove saranno valorizzate da una successiva
            // validazione server-side delle unità commerciali.

            line.setLineNo(lineNo++);
            line.setItemId(lineForm.getItemId());
            line.setDescription(lineForm.getDescription().trim());
            line.setQuantity(parseOrderQuantity(lineForm.getQuantity()));
            line.setUnitPrice(
                    scale(
                            NumberInputUtils.parseDecimal(
                                    lineForm.getUnitPrice(),
                                    "unit price"
                            ),
                            2
                    )
            );
            line.setDiscountPct(
                    scale(
                            NumberInputUtils.parseDecimal(
                                    lineForm.getDiscountPct(),
                                    "discount"
                            ),
                            2
                    )
            );
            line.setTaxPct(
                    scale(
                            NumberInputUtils.parseDecimal(
                                    lineForm.getTaxPct(),
                                    "tax pct"
                            ),
                            2
                    )
            );

            LineTotals lt = calculateLineTotals(lineForm);
            line.setLineSubtotal(lt.subtotal());
            line.setTaxAmount(lt.tax());
            line.setLineTotal(lt.total());

            purchaseOrderLineRepository.save(line);
        }
    }

    private Totals calculateTotals(List<PurchaseOrderLineForm> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (var line : lines) {
            LineTotals lt = calculateLineTotals(line);
            subtotal = subtotal.add(lt.subtotal());
            tax = tax.add(lt.tax());
            total = total.add(lt.total());
        }

        return new Totals(
                validateMonetaryCapacity(subtotal, "Subtotale ordine"),
                validateMonetaryCapacity(tax, "IVA ordine"),
                validateMonetaryCapacity(total, "Totale ordine")
        );
    }

    private LineTotals calculateLineTotals(PurchaseOrderLineForm line) {

        BigDecimal qty = parseOrderQuantity(line.getQuantity());
        BigDecimal unitPrice = scale(NumberInputUtils.parseDecimal(line.getUnitPrice(), "unit price"),2);
        BigDecimal discountPct = scale(NumberInputUtils.parseDecimal(line.getDiscountPct(),"discount"), 2);
        BigDecimal taxPct = scale(NumberInputUtils.parseDecimal(line.getTaxPct(),"tax pct" ),2);
        BigDecimal gross = qty.multiply(unitPrice);
        BigDecimal discountAmount = gross.multiply(discountPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal subtotal = gross.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(taxPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        return new LineTotals(
                validateMonetaryCapacity(subtotal, "Subtotale riga"),
                validateMonetaryCapacity(tax, "IVA riga"),
                validateMonetaryCapacity(total, "Totale riga")
        );
    }

    /**
     * Protegge gli importi destinati a colonne DECIMAL(15,2).
     * Non arrotonda: rifiuta valori non rappresentabili esattamente.
     */
    private BigDecimal validateMonetaryCapacity(
            BigDecimal amount,
            String fieldName
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                    fieldName + ": importo non valido."
            );
        }

        BigDecimal normalized;

        try {
            normalized = amount.setScale(
                    2,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    fieldName
                    + ": importo non rappresentabile esattamente "
                    + "a 2 decimali."
            );
        }

        if (normalized.precision() - normalized.scale() > 13) {
            throw new BusinessException(
                    fieldName
                    + ": importo superiore alla capacità "
                    + "massima consentita."
            );
        }

        return normalized;
    }

    private BigDecimal parseOrderQuantity(String value) {
        BigDecimal quantity = NumberInputUtils.parseDecimal(value, "quantity");

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità dell'ordine deve essere maggiore di zero."
            );
        }

        if (quantity.stripTrailingZeros().scale() > 3) {
            throw new BusinessException(
                    "La quantità dell'ordine può avere al massimo 3 decimali."
            );
        }

        BigDecimal normalized = quantity.setScale(
                3,
                RoundingMode.UNNECESSARY
        );

        if (normalized.precision() - normalized.scale() > 12) {
            throw new BusinessException(
                    "La quantità dell'ordine supera la capacità "
                    + "massima consentita."
            );
        }

        return normalized;
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value == null ? BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP)
                : value.setScale(scale, RoundingMode.HALF_UP);
    }

    private PurchaseOrderLineForm defaultLine() {
        PurchaseOrderLineForm line = new PurchaseOrderLineForm();
        line.setQuantity(PdfFormatUtils.formatDecimal(new BigDecimal("1"),0));
        line.setUnitPrice(PdfFormatUtils.formatMoney(BigDecimal.ZERO));
        line.setDiscountPct(PdfFormatUtils.formatDecimal(BigDecimal.ZERO,0));
        line.setTaxPct(PdfFormatUtils.formatDecimal(new BigDecimal("22"),0));
        line.setItemId(null);
        return line;
    }

    private String nextOrderNumber(Long tenantId, Long companyId) {
        return documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.PURCHASE_ORDER
        );
    }

    @Transactional(readOnly = true)
    public PurchaseOrderLineForm buildDefaultLine() {
        PurchaseOrderLineForm line = new PurchaseOrderLineForm();
        line.setItemId(null);
        line.setDescription("");
        line.setQuantity(PdfFormatUtils.formatDecimalTrimmed(java.math.BigDecimal.ONE,0));
        line.setUnitPrice(PdfFormatUtils.formatMoney(java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP)));
        line.setDiscountPct(PdfFormatUtils.formatDecimal(java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP)));
        line.setTaxPct(PdfFormatUtils.formatDecimal(new java.math.BigDecimal("22"),0));
        return line;
    }

    private record LineTotals(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    }

    private record Totals(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    }

    public Page<PurchaseOrderListItemView> findPage(Long tenantId,
                                                    int page,
                                                    int size,
                                                    String q,
                                                    String status,
                                                    LocalDate dateFrom,
                                                    LocalDate dateTo) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("orderDate"), Sort.Order.desc("id"))
        );
        Long companyId = companyContext.resolveCurrentCompanyId();

        Specification<PurchaseOrder> spec = Specification.where(byTenantAndCompany(tenantId, companyId))
                .and(bySearch(q))
                .and(byStatus(status))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));
        return purchaseOrderRepository.findAll(spec, pageable).map(this::toListItemView);
    }

    private Specification<PurchaseOrder> byTenantAndCompany(
            Long tenantId,
            Long companyId
    ) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("tenantId"), tenantId),
                cb.equal(root.get("companyId"), companyId)
        );
    }

    private Specification<PurchaseOrder> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            Join<Object, Object> supplier = root.join("supplier", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), like),
                    cb.like(cb.lower(supplier.get("name")), like)
            );
        };

    }

    private Specification<PurchaseOrder> byStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<PurchaseOrder> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), dateFrom);
    }

    private Specification<PurchaseOrder> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("expectedDeliveryDate"), dateTo);
    }

    private PurchaseOrderListItemView toListItemView(PurchaseOrder order) {

        PurchaseOrderListItemView v = new PurchaseOrderListItemView();
        v.setId(order.getId());
        v.setOrderNumber(order.getOrderNumber());
        v.setFormattedOrderDate(PdfFormatUtils.formatDate(order.getOrderDate()));
        v.setFormattedExpectedDeliveryDate(PdfFormatUtils.formatDate(order.getExpectedDeliveryDate()));
        v.setStatus(order.getStatus());
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(order.getTotalAmount()));
        v.setCurrencyCode(order.getCurrencyCode());
        v.setSupplierName(
                supplierRepository.findByTenantIdAndId(order.getTenantId(), order.getSupplierId())
                        .map(s -> s.getName())
                        .orElse("-")
        );
        return v;

    }
}