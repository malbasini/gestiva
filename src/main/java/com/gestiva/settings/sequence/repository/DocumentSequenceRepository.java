package com.gestiva.settings.sequence.repository;

import com.gestiva.settings.sequence.entity.DocumentSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {

    // Metodi esistenti: compatibilità temporanea

    List<DocumentSequence> findByTenantIdOrderByDocumentTypeAsc(
            Long tenantId
    );

    Optional<DocumentSequence> findByTenantIdAndDocumentType(
            Long tenantId,
            String documentType
    );

    // Gestiva V2: metodi multi-società

    List<DocumentSequence> findByTenantIdAndCompanyIdOrderByDocumentTypeAsc(
            Long tenantId,
            Long companyId
    );

    Optional<DocumentSequence> findByTenantIdAndCompanyIdAndDocumentType(
            Long tenantId,
            Long companyId,
            String documentType
    );

    // Blocco pessimista per assegnare numeri progressivi senza duplicazioni

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ds
            FROM DocumentSequence ds
            WHERE ds.tenantId = :tenantId
              AND ds.companyId = :companyId
              AND ds.documentType = :documentType
            """)
    Optional<DocumentSequence> findForUpdate(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("documentType") String documentType
    );
}