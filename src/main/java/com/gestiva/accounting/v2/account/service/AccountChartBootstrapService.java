package com.gestiva.accounting.v2.account.service;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountChartBootstrapService {

    private final AccountRepository accountRepository;

    public AccountChartBootstrapService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void initializeDefaultChartOfAccounts(Long tenantId) {
        if (!accountRepository.findByTenantIdOrderByCodeAsc(tenantId).isEmpty()) {
            return;
        }

        create(tenantId, "1000", "Attività", "ASSET", "DEBIT", null, 1, false, true, true);
        create(tenantId, "1100", "Disponibilità liquide", "ASSET", "DEBIT", "1000", 2, false, true, true);
        create(tenantId, "1110", "Cassa", "ASSET", "DEBIT", "1100", 3, true, true, true);
        create(tenantId, "1120", "Banca", "ASSET", "DEBIT", "1100", 3, true, true, true);

        create(tenantId, "1200", "Crediti commerciali", "ASSET", "DEBIT", "1000", 2, false, true, true);
        create(tenantId, "1210", "Crediti verso clienti", "ASSET", "DEBIT", "1200", 3, true, true, true);

        create(tenantId, "2000", "Passività", "LIABILITY", "CREDIT", null, 1, false, true, true);
        create(tenantId, "2100", "Debiti commerciali", "LIABILITY", "CREDIT", "2000", 2, false, true, true);
        create(tenantId, "2110", "Debiti verso fornitori", "LIABILITY", "CREDIT", "2100", 3, true, true, true);

        create(tenantId, "2200", "Debiti tributari", "LIABILITY", "CREDIT", "2000", 2, false, true, true);
        create(tenantId, "2210", "IVA a debito", "LIABILITY", "CREDIT", "2200", 3, true, true, true);
        create(tenantId, "2220", "IVA a credito", "ASSET", "DEBIT", "1000", 2, true, true, true);

        create(tenantId, "3000", "Patrimonio netto", "EQUITY", "CREDIT", null, 1, true, true, true);

        create(tenantId, "4000", "Ricavi", "REVENUE", "CREDIT", null, 1, false, true, true);
        create(tenantId, "4100", "Ricavi da vendite", "REVENUE", "CREDIT", "4000", 2, true, true, true);

        create(tenantId, "5000", "Costi", "COST", "DEBIT", null, 1, false, true, true);
        create(tenantId, "5100", "Acquisti merci", "COST", "DEBIT", "5000", 2, true, true, true);
        create(tenantId, "5200", "Costi per servizi", "COST", "DEBIT", "5000", 2, true, true, true);
        create(tenantId, "5300", "Spese bancarie", "COST", "DEBIT", "5000", 2, true, true, true);
    }

    private void create(Long tenantId,
                        String code,
                        String name,
                        String accountType,
                        String nature,
                        String parentCode,
                        int levelNo,
                        boolean leaf,
                        boolean systemAccount,
                        boolean active) {

        Long parentId = null;
        if (parentCode != null) {
            parentId = accountRepository.findByTenantIdAndCode(tenantId, parentCode)
                    .map(Account::getId)
                    .orElse(null);
        }

        Account a = new Account();
        a.setTenantId(tenantId);
        a.setCode(code);
        a.setName(name);
        a.setAccountType(accountType);
        a.setNature(nature);
        a.setParentId(parentId);
        a.setLevelNo(levelNo);
        a.setLeafAccount(leaf);
        a.setSystemAccount(systemAccount);
        a.setActive(active);
        a.setDescription(null);

        accountRepository.save(a);
    }
}
