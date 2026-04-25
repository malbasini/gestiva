package com.gestiva.sales.sequence.repository;

import com.gestiva.sales.sequence.entity.DocumentSequence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {

    Optional<DocumentSequence> findByTenantIdAndSequenceCodeAndYearValue(Long tenantId,
                                                                         String sequenceCode,
                                                                         Integer yearValue);
}