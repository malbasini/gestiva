package com.gestiva.purchasing.receipt.controller;

import com.gestiva.purchasing.receipt.service.GoodsReceiptPartialApplicationService;
import com.gestiva.security.usercontext.TenantContext;

import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/purchase-orders")
public class GoodsReceiptPartialPageController {

    private static final String QUANTITY_PREFIX = "quantity_";

    private final GoodsReceiptPartialApplicationService service;
    private final TenantContext tenantContext;

    public GoodsReceiptPartialPageController(
            GoodsReceiptPartialApplicationService service,
            TenantContext tenantContext) {

        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{id}/receive-partial")
    public String receivePartial(
            @PathVariable Long id,
            @RequestParam MultiValueMap<String, String> parameters,
            RedirectAttributes redirectAttributes) {

        try {
            Long warehouseId = parsePositiveId(
                    singleValue(parameters, "warehouseId"),
                    "Magazzino non valido."
            );

            Map<Long, BigDecimal> quantities =
                    parseQuantities(parameters);

            if (quantities.isEmpty()) {
                throw new IllegalArgumentException(
                        "Indicare almeno una quantità da ricevere."
                );
            }

            Long tenantId = tenantContext.getCurrentTenantId();

            Long receiptId = service.create(
                    tenantId,
                    id,
                    warehouseId,
                    quantities
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Ricevimento parziale creato con successo."
            );

            return "redirect:/goods-receipts/" + receiptId;

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

            return "redirect:/purchase-orders/" + id;
        }
    }

    private static Map<Long, BigDecimal> parseQuantities(
            MultiValueMap<String, String> parameters) {

        Map<Long, BigDecimal> quantities = new HashMap<>();

        for (String name : parameters.keySet()) {
            if (!name.startsWith(QUANTITY_PREFIX)) {
                continue;
            }

            String idPart = name.substring(QUANTITY_PREFIX.length());

            Long orderLineId = parsePositiveId(
                    idPart,
                    "Identificativo riga ordine non valido."
            );

            String raw = singleValue(parameters, name);

            if (raw == null || raw.isBlank()) {
                continue;
            }

            BigDecimal quantity;

            try {
                quantity = new BigDecimal(
                        raw.trim().replace(',', '.')
                );
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Quantità del ricevimento non valida."
                );
            }

            if (quantity.signum() < 0) {
                throw new IllegalArgumentException(
                        "Le quantità non possono essere negative."
                );
            }

            if (quantity.signum() == 0) {
                continue;
            }

            if (quantities.putIfAbsent(orderLineId, quantity) != null) {
                throw new IllegalArgumentException(
                        "Riga ordine duplicata."
                );
            }
        }

        return quantities;
    }

    private static String singleValue(
            MultiValueMap<String, String> parameters,
            String name) {

        var values = parameters.get(name);

        if (values == null || values.size() != 1) {
            throw new IllegalArgumentException(
                    "Parametro mancante o duplicato: " + name
            );
        }

        return values.getFirst();
    }

    private static Long parsePositiveId(
            String raw,
            String message) {

        try {
            Long value = Long.valueOf(raw);

            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException ex) {
            // Identificativo non valido.
        }

        throw new IllegalArgumentException(message);
    }
}
