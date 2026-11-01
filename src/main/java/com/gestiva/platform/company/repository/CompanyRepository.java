package com.gestiva.platform.company.repository;

import com.gestiva.platform.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByTenantIdAndCode(
            Long tenantId,
            String code
    );

    Optional<Company> findByTenantIdAndId(
            Long tenantId,
            Long id
    );
    
    
    
@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM Company c
            WHERE c.tenantId = :tenantId
              AND c.id = :id
            """)
    Optional<Company> findByTenantIdAndIdForUpdate(
            @Param("tenantId") Long tenantId,
            @Param("id") Long id
    );



    List<Company> findByTenantIdAndActiveTrueOrderByLegalNameAsc(
            Long tenantId
    );

    // Gestione amministrativa: include società attive e inattive.

    // Blocca le società del tenant durante le modifiche
    // amministrative che richiedono verifiche globali.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM Company c
            WHERE c.tenantId = :tenantId
            ORDER BY c.id ASC
            """)
    List<Company> findAllByTenantIdForUpdate(
            @Param("tenantId") Long tenantId
    );

    List<Company> findByTenantIdOrderByLegalNameAsc(
            Long tenantId
    );



}
