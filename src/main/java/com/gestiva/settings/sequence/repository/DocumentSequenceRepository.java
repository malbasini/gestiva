package com.gestiva.settings.sequence.repository;

import com.gestiva.settings.sequence.entity.DocumentSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {

    List<DocumentSequence> findByTenantIdOrderByDocumentTypeAsc(Long tenantId);

    Optional<DocumentSequence> findByTenantIdAndDocumentType(Long tenantId, String documentType);
}