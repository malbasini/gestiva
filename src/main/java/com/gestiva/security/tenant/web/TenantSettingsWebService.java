package com.gestiva.security.tenant.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.inventory.valuation.model.InventoryValuationMethod;
import com.gestiva.security.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TenantSettingsWebService {

    private final TenantRepository tenantRepository;

    public TenantSettingsWebService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public TenantInventoryValuationForm getInventoryValuationForm(Long tenantId) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("Tenant non trovato."));

        TenantInventoryValuationForm form = new TenantInventoryValuationForm();
        form.setInventoryValuationMethod(
                tenant.getInventoryValuationMethod() == null ? "FIFO" : String.valueOf(tenant.getInventoryValuationMethod())

        );
        return form;
    }

    public void updateInventoryValuationMethod(Long tenantId, TenantInventoryValuationForm form) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("Tenant non trovato."));

        String method = form.getInventoryValuationMethod();
        if (method == null || method.isBlank()) {
            throw new BusinessException("Metodo di valorizzazione non valido.");
        }

        method = method.trim().toUpperCase();

        if (!"FIFO".equals(method) && !"LIFO".equals(method) && !"AVERAGE".equals(method)) {
            throw new BusinessException("Metodo di valorizzazione non valido: " + method);
        }
        tenant.setInventoryValuationMethod(InventoryValuationMethod.valueOf(method));
        tenantRepository.save(tenant);
    }
}