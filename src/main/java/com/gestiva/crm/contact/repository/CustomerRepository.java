package com.gestiva.crm.contact.repository;

import com.gestiva.crm.contact.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    List<Customer> findByTenantIdOrderByNameAsc(Long tenantId);
    Optional<Customer> findByTenantIdAndId(Long tenantId, Long id);
    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);
}