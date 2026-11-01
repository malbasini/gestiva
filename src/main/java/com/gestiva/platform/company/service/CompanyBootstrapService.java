package com.gestiva.platform.company.service;

import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.accounting.v2.account.service.AccountChartBootstrapService;
import com.gestiva.platform.company.entity.CompanySettings;
import com.gestiva.settings.company.repository.CompanySettingsRepository;
import java.math.BigDecimal;
import com.gestiva.settings.sequence.entity.DocumentSequence;
import com.gestiva.settings.sequence.repository.DocumentSequenceRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyBootstrapService {

    private final CompanyRepository companyRepository;
    private final CompanySettingsRepository companySettingsRepository;
    private final DocumentSequenceRepository documentSequenceRepository;
    private final AccountChartBootstrapService accountChartBootstrapService;

    public CompanyBootstrapService(
            CompanyRepository companyRepository,
            CompanySettingsRepository companySettingsRepository,
            DocumentSequenceRepository documentSequenceRepository,
            AccountChartBootstrapService accountChartBootstrapService) {

        this.companyRepository = companyRepository;
        this.companySettingsRepository = companySettingsRepository;
        this.documentSequenceRepository = documentSequenceRepository;
        this.accountChartBootstrapService = accountChartBootstrapService;
    }

    public void initializeCompany(Long tenantId, Long companyId) {

        if (tenantId == null || companyId == null) {
            throw new IllegalArgumentException(
                    "Tenant e società sono obbligatori."
            );
        }

        companyRepository.findByTenantIdAndIdForUpdate(
                tenantId, companyId
        ).orElseThrow(() -> new IllegalArgumentException(
                "Società non trovata per il tenant indicato."
        ));

        initializeDefaultSettings(tenantId, companyId);
        initializeDefaultSequences(tenantId, companyId);

        accountChartBootstrapService.initializeDefaultChartOfAccounts(
                tenantId, companyId);
    }

    public void initializeDefaultSettings(Long tenantId, Long companyId) {

        Company company = requireCompany(tenantId, companyId);

        if (companySettingsRepository
                .findByTenantIdAndCompanyId(tenantId, companyId)
                .isPresent()) {
            return;
        }

        CompanySettings settings = new CompanySettings();
        settings.setTenantId(tenantId);
        settings.setCompany(company);
        settings.setDefaultVatPct(new BigDecimal("22.00"));
        settings.setDefaultCustomerDueDays(30);
        settings.setDefaultSupplierDueDays(30);

        companySettingsRepository.save(settings);
    }

    private static final List<String> DEFAULT_DOCUMENT_TYPES = List.of(
            "QUOTE",
            "SALES_ORDER",
            "DELIVERY_NOTE",
            "SALES_INVOICE",
            "PURCHASE_ORDER",
            "GOODS_RECEIPT",
            "SUPPLIER_INVOICE",
            "JOURNAL_ENTRY"
    );

    public void initializeDefaultSequences(Long tenantId, Long companyId) {

        requireCompany(tenantId, companyId);

        Set<String> existingTypes = documentSequenceRepository
                .findByTenantIdAndCompanyIdOrderByDocumentTypeAsc(
                        tenantId, companyId)
                .stream()
                .map(DocumentSequence::getDocumentType)
                .collect(Collectors.toSet());

        for (String type : DEFAULT_DOCUMENT_TYPES) {

            if (existingTypes.contains(type)) {
                continue;
            }

            DocumentSequence sequence = new DocumentSequence();
            sequence.setTenantId(tenantId);
            sequence.setCompanyId(companyId);
            sequence.setDocumentType(type);
            sequence.setPrefix(defaultPrefix(type));
            sequence.setNextNumber(1L);
            sequence.setPaddingSize(5);

            documentSequenceRepository.save(sequence);
        }
    }

    private String defaultPrefix(String type) {
        return switch (type) {
            case "QUOTE" -> "QUO-";
            case "SALES_ORDER" -> "SO-";
            case "DELIVERY_NOTE" -> "DDT-";
            case "SALES_INVOICE" -> "INV-";
            case "PURCHASE_ORDER" -> "PO-";
            case "GOODS_RECEIPT" -> "GR-";
            case "SUPPLIER_INVOICE" -> "SI-";
            case "JOURNAL_ENTRY" -> "JE-";
            default -> throw new IllegalArgumentException(
                    "Tipo documento non supportato: " + type);
        };
    }

    private Company requireCompany(Long tenantId, Long companyId) {

        if (tenantId == null || companyId == null) {
            throw new IllegalArgumentException(
                    "Tenant e società sono obbligatori."
            );
        }

        return companyRepository
                .findByTenantIdAndId(tenantId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Società non trovata per il tenant indicato."
                ));
    }
}
