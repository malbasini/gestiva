package com.gestiva.accounting.v2.account.web;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AccountWebService {

    private final AccountRepository accountRepository;

    public AccountWebService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<AccountListItemView> findAll(Long tenantId) {
        List<Account> accounts = accountRepository.findByTenantIdOrderByCodeAsc(tenantId);

        Map<Long, Account> byId = new HashMap<>();
        for (Account account : accounts) {
            byId.put(account.getId(), account);
        }

        return accounts.stream().map(account -> {
            AccountListItemView v = new AccountListItemView();
            v.setId(account.getId());
            v.setCode(account.getCode());
            v.setName(account.getName());
            v.setAccountType(account.getAccountType());
            v.setNature(account.getNature());
            v.setLevelNo(account.getLevelNo());
            v.setLeafAccount(account.isLeafAccount());
            v.setSystemAccount(account.isSystemAccount());
            v.setActive(account.isActive());

            if (account.getParentId() != null) {
                Account parent = byId.get(account.getParentId());
                if (parent != null) {
                    v.setParentCode(parent.getCode());
                    v.setParentName(parent.getName());
                }
            }

            v.setIndentedName(indent(account.getLevelNo()) + account.getName());

            return v;
        }).toList();
    }

    private String indent(Integer levelNo) {
        if (levelNo == null || levelNo <= 1) {
            return "";
        }
        return "— ".repeat(levelNo - 1);
    }

    public java.util.List<AccountOptionView> findLeafOptions(Long tenantId) {
        return accountRepository.findByTenantIdAndLeafAccountTrueAndActiveTrueOrderByCodeAsc(tenantId)
                .stream()
                .map(account -> {
                    AccountOptionView v = new AccountOptionView();
                    v.setId(account.getId());
                    v.setCode(account.getCode());
                    v.setName(account.getName());
                    v.setLabel(account.getCode() + " - " + account.getName());
                    return v;
                })
                .toList();
    }
}