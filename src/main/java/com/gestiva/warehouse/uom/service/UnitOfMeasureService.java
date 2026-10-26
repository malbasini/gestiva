package com.gestiva.warehouse.uom.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import com.gestiva.warehouse.uom.entity.UnitOfMeasureDimension;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository repository;

    public UnitOfMeasureService(UnitOfMeasureRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<UnitOfMeasure> findAll(Long tenantId) {
        validateTenant(tenantId);
        return repository.findByTenantId(tenantId);
    }

    @Transactional
    public UnitOfMeasure create(
            Long tenantId,
            String code,
            String name,
            String symbol,
            UnitOfMeasureDimension dimension,
            Integer decimalPlaces
    ) {
        validateTenant(tenantId);

        String normalizedCode = normalizeCode(code);
        String normalizedName = normalizeRequired(name, "Descrizione", 100);
        String normalizedSymbol = normalizeRequired(symbol, "Simbolo", 20);

        if (dimension == null) {
            throw new BusinessException(
                    "La dimensione dell'unità di misura è obbligatoria."
            );
        }

        if (decimalPlaces == null || decimalPlaces < 0 || decimalPlaces > 6) {
            throw new BusinessException(
                    "Il numero di decimali deve essere compreso tra 0 e 6."
            );
        }

        if (repository.existsByTenantIdAndCode(tenantId, normalizedCode)) {
            throw new BusinessException(
                    "Esiste già un'unità di misura con codice "
                    + normalizedCode + "."
            );
        }

        UnitOfMeasure uom = new UnitOfMeasure();
        uom.setTenantId(tenantId);
        uom.setCode(normalizedCode);
        uom.setName(normalizedName);
        uom.setSymbol(normalizedSymbol);
        uom.setDimension(dimension);
        uom.setDecimalPlaces(decimalPlaces);
        uom.setActive(true);

        return repository.save(uom);
    }

    private void validateTenant(Long tenantId) {
        if (tenantId == null || tenantId <= 0) {
            throw new BusinessException("Tenant non valido.");
        }
    }

    private String normalizeCode(String value) {
        String code = normalizeRequired(value, "Codice", 20)
                .toUpperCase(Locale.ROOT);

        if (!code.matches("[A-Z0-9_-]+")) {
            throw new BusinessException(
                    "Il codice può contenere soltanto lettere, "
                    + "numeri, trattini e underscore."
            );
        }

        return code;
    }

    private String normalizeRequired(
            String value,
            String fieldName,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    fieldName + " obbligatorio."
            );
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            throw new BusinessException(
                    fieldName + ": massimo " + maxLength + " caratteri."
            );
        }

        return normalized;
    }
}
