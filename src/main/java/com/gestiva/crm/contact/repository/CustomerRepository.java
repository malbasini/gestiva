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
    long countByTenantId(Long tenantId);

    List<Customer> findByTenantIdAndCompanyIdOrderByNameAsc(
            Long tenantId,
            Long companyId
    );

    Optional<Customer> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    boolean existsByTenantIdAndCompanyIdAndNameIgnoreCase(
            Long tenantId,
            Long companyId,
            String name
    );

    long countByTenantIdAndCompanyId(
            Long tenantId,
            Long companyId
    );
}