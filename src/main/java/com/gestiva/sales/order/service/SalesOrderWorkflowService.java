package com.gestiva.sales.order.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SalesOrderWorkflowService {

    private final SalesOrderRepository salesOrderRepository;
    private final CompanyContext companyContext;

    public SalesOrderWorkflowService(
            SalesOrderRepository salesOrderRepository,
            CompanyContext companyContext) {

        this.salesOrderRepository = salesOrderRepository;
        this.companyContext = companyContext;
    }

    private Long requireCompanyId() {

        Long companyId = companyContext.resolveCurrentCompanyId();

        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata.");
        }

        return companyId;
    }

    public void markFulfilled(Long tenantId, Long orderId) {

        Long companyId = requireCompanyId();

        SalesOrder order = salesOrderRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        orderId)
                .orElseThrow(() -> new NotFoundException(
                        "Ordine non trovato nella società attiva"));

        if ("FULFILLED".equals(order.getStatus())) {
            throw new BusinessException("L'ordine è già evaso.");
        }

        if ("CANCELLED".equals(order.getStatus())) {
            throw new BusinessException(
                    "L'ordine è annullato e non può essere evaso.");
        }

        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException(
                    "Solo un ordine CONFIRMED può essere evaso.");
        }

        order.setStatus("FULFILLED");

        salesOrderRepository.save(order);
    }

    public void cancel(Long tenantId, Long orderId) {

        Long companyId = requireCompanyId();

        SalesOrder order = salesOrderRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        orderId)
                .orElseThrow(() -> new NotFoundException(
                        "Ordine non trovato nella società attiva"));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new BusinessException("L'ordine è già annullato.");
        }

        if ("FULFILLED".equals(order.getStatus())) {
            throw new BusinessException(
                    "Un ordine evaso non può essere annullato.");
        }

        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException(
                    "Solo un ordine CONFIRMED può essere annullato.");
        }

        order.setStatus("CANCELLED");

        salesOrderRepository.save(order);
    }
}