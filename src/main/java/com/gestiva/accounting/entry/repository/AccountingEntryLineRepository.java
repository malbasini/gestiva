package com.gestiva.accounting.entry.repository;

import com.gestiva.accounting.entry.entity.AccountingEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountingEntryLineRepository extends JpaRepository<AccountingEntryLine, Long> {

    List<AccountingEntryLine> findByTenantIdAndAccountingEntryIdOrderByLineNoAsc(Long tenantId, Long accountingEntryId);
}