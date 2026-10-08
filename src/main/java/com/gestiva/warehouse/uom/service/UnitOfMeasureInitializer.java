package com.gestiva.warehouse.uom.service;

import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import com.gestiva.warehouse.uom.entity.UnitOfMeasureDimension;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UnitOfMeasureInitializer {

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public UnitOfMeasureInitializer(
            UnitOfMeasureRepository unitOfMeasureRepository) {

        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Transactional
    public void initializeForTenant(Long tenantId) {

        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId obbligatorio");
        }

        Set<String> existingCodes = new HashSet<>();

        for (UnitOfMeasure uom :
                unitOfMeasureRepository.findByTenantId(tenantId)) {

            if (uom.getCode() != null) {
                existingCodes.add(
                        uom.getCode()
                                .trim()
                                .toUpperCase(Locale.ROOT)
                );
            }
        }

        for (DefaultUom definition : defaultUoms()) {

            if (existingCodes.contains(definition.code())) {
                continue;
            }

            UnitOfMeasure uom = new UnitOfMeasure();

            uom.setTenantId(tenantId);
            uom.setCode(definition.code());
            uom.setName(definition.name());
            uom.setSymbol(definition.symbol());
            uom.setDimension(definition.dimension());
            uom.setDecimalPlaces(definition.decimalPlaces());
            uom.setActive(true);

            unitOfMeasureRepository.save(uom);
        }
    }

    private List<DefaultUom> defaultUoms() {
        return List.of(
                new DefaultUom(
                        "PZ",
                        "Pezzo",
                        "pz",
                        UnitOfMeasureDimension.UNIT,
                        0
                ),
                new DefaultUom(
                        "KG",
                        "Chilogrammo",
                        "kg",
                        UnitOfMeasureDimension.MASS,
                        3
                ),
                new DefaultUom(
                        "G",
                        "Grammo",
                        "g",
                        UnitOfMeasureDimension.MASS,
                        3
                ),
                new DefaultUom(
                        "L",
                        "Litro",
                        "l",
                        UnitOfMeasureDimension.VOLUME,
                        3
                ),
                new DefaultUom(
                        "ML",
                        "Millilitro",
                        "ml",
                        UnitOfMeasureDimension.VOLUME,
                        3
                ),
                new DefaultUom(
                        "M",
                        "Metro",
                        "m",
                        UnitOfMeasureDimension.LENGTH,
                        3
                ),
                new DefaultUom(
                        "M2",
                        "Metro quadrato",
                        "m²",
                        UnitOfMeasureDimension.AREA,
                        3
                ),
                new DefaultUom(
                        "H",
                        "Ora",
                        "h",
                        UnitOfMeasureDimension.TIME,
                        2
                )
        );
    }

    private record DefaultUom(
            String code,
            String name,
            String symbol,
            UnitOfMeasureDimension dimension,
            int decimalPlaces) {
    }
}