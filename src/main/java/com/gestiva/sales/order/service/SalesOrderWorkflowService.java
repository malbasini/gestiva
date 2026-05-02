package com.gestiva.sales.order.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SalesOrderWorkflowService {

    private final SalesOrderRepository salesOrderRepository;

    public SalesOrderWorkflowService(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
    }

    public void markFulfilled(Long tenantId, Long orderId) {
        SalesOrder order = salesOrderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new NotFoundException("Ordine non trovato"));

        if ("FULFILLED".equals(order.getStatus())) {
            throw new BusinessException("L'ordine è già evaso.");
        }

        if ("CANCELLED".equals(order.getStatus())) {
            throw new BusinessException("L'ordine è annullato e non può essere evaso.");
        }

        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException("Solo un ordine CONFIRMED può essere evaso.");
        }

        order.setStatus("FULFILLED");
        salesOrderRepository.save(order);
    }

    public void cancel(Long tenantId, Long orderId) {
        SalesOrder order = salesOrderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new NotFoundException("Ordine non trovato"));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new BusinessException("L'ordine è già annullato.");
        }

        if ("FULFILLED".equals(order.getStatus())) {
            throw new BusinessException("Un ordine evaso non può essere annullato.");
        }

        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException("Solo un ordine CONFIRMED può essere annullato.");
        }

        order.setStatus("CANCELLED");
        salesOrderRepository.save(order);
    }
}