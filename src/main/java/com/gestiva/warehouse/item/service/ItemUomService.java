package com.gestiva.warehouse.item.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.entity.ItemUom;
import com.gestiva.warehouse.item.repository.ItemUomRepository;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class ItemUomService {

    private static final int QUANTITY_SCALE = 6;

    private final ItemRepository itemRepository;
    private final ItemUomRepository itemUomRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public ItemUomService(
            ItemRepository itemRepository,
            ItemUomRepository itemUomRepository,
            UnitOfMeasureRepository unitOfMeasureRepository) {

        this.itemRepository = itemRepository;
        this.itemUomRepository = itemUomRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    /**
     * Associa un'unità di misura alternativa a un articolo.
     */
    @Transactional
    public ItemUom create(
            Long tenantId,
            Long itemId,
            Long uomId,
            BigDecimal conversionFactor,
            boolean purchaseUom,
            boolean salesUom,
            String barcode) {

        Item item = getItem(tenantId, itemId);
        UnitOfMeasure uom = getUom(tenantId, uomId);

        validateConversion(item, uom, conversionFactor);

        if (itemUomRepository.existsByTenantIdAndItemIdAndUomId(
                tenantId, itemId, uomId)) {

            throw new BusinessException(
                    "L'unità di misura è già associata all'articolo."
            );
        }

        ItemUom itemUom = new ItemUom();

        itemUom.setTenantId(tenantId);
        itemUom.setItem(item);
        itemUom.setUom(uom);
        itemUom.setConversionFactor(conversionFactor);
        itemUom.setPurchaseUom(purchaseUom);
        itemUom.setSalesUom(salesUom);
        itemUom.setBarcode(normalizeBarcode(barcode));
        itemUom.setActive(true);

        return itemUomRepository.save(itemUom);
    }

    /**
     * Converte una quantità nell'unità di misura base dell'articolo.
     *
     * Esempio:
     * 3 SC × 100 = 300 PZ
     */
    public BigDecimal convertToBase(
            Long tenantId,
            Long itemId,
            Long uomId,
            BigDecimal quantity) {

        Item item = getItem(tenantId, itemId);
        UnitOfMeasure uom = getUom(tenantId, uomId);

        validateQuantity(quantity);
        validateDecimalPlaces(quantity, uom);

        UnitOfMeasure baseUom = item.getBaseUom();

        if (baseUom.getId().equals(uom.getId())) {
            return quantity;
        }

        ItemUom itemUom = itemUomRepository
                .findByTenantIdAndItemIdAndUomId(
                        tenantId, itemId, uomId
                )
                .orElseThrow(() -> new BusinessException(
                        "Conversione non configurata per l'articolo."
                ));

        if (!itemUom.isActive()) {
            throw new BusinessException(
                    "La conversione non è attiva."
            );
        }

        BigDecimal baseQuantity = quantity.multiply(
                itemUom.getConversionFactor()
        );

        validateDecimalPlaces(baseQuantity, baseUom);

        return baseQuantity.setScale(
                QUANTITY_SCALE,
                RoundingMode.UNNECESSARY
        );
    }

    /**
     * Converte una quantità base in un'unità alternativa.
     *
     * Esempio:
     * 300 PZ / 100 = 3 SC
     */
    public BigDecimal convertFromBase(
            Long tenantId,
            Long itemId,
            Long uomId,
            BigDecimal baseQuantity) {

        Item item = getItem(tenantId, itemId);
        UnitOfMeasure uom = getUom(tenantId, uomId);

        validateQuantity(baseQuantity);

        UnitOfMeasure baseUom = item.getBaseUom();

        validateDecimalPlaces(baseQuantity, baseUom);

        if (baseUom.getId().equals(uom.getId())) {
            return baseQuantity;
        }

        ItemUom itemUom = itemUomRepository
                .findByTenantIdAndItemIdAndUomId(
                        tenantId, itemId, uomId
                )
                .orElseThrow(() -> new BusinessException(
                        "Conversione non configurata per l'articolo."
                ));

        if (!itemUom.isActive()) {
            throw new BusinessException(
                    "La conversione non è attiva."
            );
        }

        BigDecimal convertedQuantity;

        try {
            convertedQuantity = baseQuantity.divide(
                    itemUom.getConversionFactor(),
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "La conversione non produce una quantità esatta."
            );
        }

        validateDecimalPlaces(convertedQuantity, uom);

        return convertedQuantity;
    }

    private Item getItem(Long tenantId, Long itemId) {
        return itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new BusinessException(
                        "Articolo non trovato."
                ));
    }

    private UnitOfMeasure getUom(Long tenantId, Long uomId) {
        return unitOfMeasureRepository.findByTenantIdAndId(
                        tenantId, uomId
                )
                .orElseThrow(() -> new BusinessException(
                        "Unità di misura non trovata."
                ));
    }

    private void validateConversion(
            Item item,
            UnitOfMeasure uom,
            BigDecimal conversionFactor) {

        if (conversionFactor == null ||
                conversionFactor.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Il fattore di conversione deve essere positivo."
            );
        }

        if (conversionFactor.stripTrailingZeros().scale() > 6) {
            throw new BusinessException(
                    "Il fattore di conversione può avere al massimo 6 decimali."
            );
        }

        if (!uom.isActive()) {
            throw new BusinessException(
                    "L'unità di misura non è attiva."
            );
        }

        if (item.getBaseUom().getId().equals(uom.getId())) {
            throw new BusinessException(
                    "Non è necessario configurare una conversione per l'unità base."
            );
        }

        if (item.getBaseUom().getDimension() != uom.getDimension()) {
            throw new BusinessException(
                    "Le unità di misura devono appartenere alla stessa dimensione."
            );
        }

        if (!item.isActive()) {
            throw new BusinessException(
                    "Non è possibile configurare conversioni per un articolo inattivo."
            );
        }

        if (!item.getBaseUom().isActive()) {
            throw new BusinessException(
                    "L'unità di misura base dell'articolo non è attiva."
            );
        }
    }

    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null ||
                quantity.compareTo(BigDecimal.ZERO) < 0) {

            throw new BusinessException(
                    "La quantità non può essere negativa."
            );
        }
    }

    private String normalizeBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return null;
        }

        return barcode.trim();
    }

    private void validateDecimalPlaces(
            BigDecimal quantity,
            UnitOfMeasure uom) {

        if (quantity == null) {
            throw new BusinessException("Quantità obbligatoria.");
        }

        if (quantity.stripTrailingZeros().scale() > uom.getDecimalPlaces()) {
            throw new BusinessException(
                    "La quantità non rispetta i decimali consentiti per l'unità "
                            + uom.getCode()
            );
        }
    }
}