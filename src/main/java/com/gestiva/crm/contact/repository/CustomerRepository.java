package com.gestiva.crm.contact.repository;

import com.gestiva.crm.contact.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByTenantIdOrderByNameAsc(Long tenantId);

    Optional<Customer> findByTenantIdAndId(Long tenantId, Long id);

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);
}