package com.gestiva.settings.company.service;

import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.settings.company.repository.CompanySettingsRepository;
import com.gestiva.settings.company.web.CompanySettingsForm;
import com.gestiva.settings.company.web.DocumentSequenceForm;
import com.gestiva.settings.sequence.entity.DocumentSequence;
import com.gestiva.settings.sequence.repository.DocumentSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestiva.platform.company.entity.CompanySettings;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanySettingsService {

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

    private final CompanySettingsRepository companySettingsRepository;
    private final DocumentSequenceRepository documentSequenceRepository;
    private final CompanyRepository companyRepository;

    public CompanySettingsService(CompanySettingsRepository companySettingsRepository,
                                  DocumentSequenceRepository documentSequenceRepository,
                                  CompanyRepository companyRepository) {

        this.companySettingsRepository = companySettingsRepository;
        this.documentSequenceRepository = documentSequenceRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CompanySettingsForm getOrCreateForm(Long tenantId) {
        Company company = getMainCompany(tenantId);

        CompanySettings settings =
                companySettingsRepository
                        .findByTenantIdAndCompanyId(tenantId, company.getId())
                        .orElseGet(() -> createDefaultSettings(tenantId, company));

        ensureDefaultSequences(tenantId);

        List<DocumentSequence> sequences = documentSequenceRepository.findByTenantIdOrderByDocumentTypeAsc(tenantId);

        CompanySettingsForm form = new CompanySettingsForm();
        form.setCompanyName(company.getLegalName());
        form.setTradeName(company.getTradeName());
        form.setVatNumber(company.getVatNumber());
        form.setTaxCode(company.getTaxCode());
        form.setEmail(company.getEmail());
        form.setPhone(company.getPhone());
        form.setWebsite(company.getWebsite());
        form.setAddressLine1(company.getAddressLine1());
        form.setPostalCode(company.getPostalCode());
        form.setCity(company.getCity());
        form.setProvince(company.getProvince());
        form.setCountryCode(company.getCountryCode());
        form.setDefaultCurrencyCode(company.getCurrencyCode());
        form.setDefaultVatPct(settings.getDefaultVatPct());
        form.setDefaultCustomerDueDays(settings.getDefaultCustomerDueDays());
        form.setDefaultSupplierDueDays(settings.getDefaultSupplierDueDays());
        List<DocumentSequenceForm> sequenceForms = new ArrayList<>();
        for (DocumentSequence seq : sequences) {
            DocumentSequenceForm sf = new DocumentSequenceForm();
            sf.setDocumentType(seq.getDocumentType());
            sf.setPrefix(seq.getPrefix());
            sf.setNextNumber(seq.getNextNumber());
            sf.setPaddingSize(seq.getPaddingSize());
            sequenceForms.add(sf);
        }
        form.setSequences(sequenceForms);

        return form;
    }

    public void save(Long tenantId, CompanySettingsForm form) {
        Company company = getMainCompany(tenantId);

        com.gestiva.platform.company.entity.CompanySettings settings =
                companySettingsRepository
                        .findByTenantIdAndCompanyId(tenantId, company.getId())
                        .orElseGet(() -> createDefaultSettings(tenantId, company));
        company.setLegalName(trimToNull(form.getCompanyName()));
        company.setTradeName(trimToNull(form.getTradeName()));
        company.setVatNumber(trimToNull(form.getVatNumber()));
        company.setTaxCode(trimToNull(form.getTaxCode()));
        company.setEmail(trimToNull(form.getEmail()));
        company.setPhone(trimToNull(form.getPhone()));
        company.setWebsite(trimToNull(form.getWebsite()));
        company.setAddressLine1(trimToNull(form.getAddressLine1()));
        company.setPostalCode(trimToNull(form.getPostalCode()));
        company.setCity(trimToNull(form.getCity()));
        company.setProvince(trimToNull(form.getProvince()));
        company.setCountryCode(trimToNull(form.getCountryCode()));
        company.setCurrencyCode(
                defaultIfBlank(form.getDefaultCurrencyCode(), "EUR")
        );

        companyRepository.save(company);

        settings.setDefaultVatPct(
                form.getDefaultVatPct() != null
                        ? form.getDefaultVatPct()
                        : BigDecimal.ZERO
        );

        settings.setDefaultCustomerDueDays(
                form.getDefaultCustomerDueDays()
        );

        settings.setDefaultSupplierDueDays(
                form.getDefaultSupplierDueDays()
        );

        companySettingsRepository.save(settings);

        Map<String, DocumentSequence> existingByType = documentSequenceRepository.findByTenantIdOrderByDocumentTypeAsc(tenantId)
                .stream()
                .collect(Collectors.toMap(DocumentSequence::getDocumentType, Function.identity()));

        for (DocumentSequenceForm sf : form.getSequences()) {
            if (sf.getDocumentType() == null || sf.getDocumentType().isBlank()) {
                continue;
            }

            DocumentSequence seq = existingByType.get(sf.getDocumentType());
            if (seq == null) {
                seq = new DocumentSequence();
                seq.setTenantId(tenantId);
                seq.setDocumentType(sf.getDocumentType());
            }

            seq.setPrefix(trimToNull(sf.getPrefix()));
            seq.setNextNumber(sf.getNextNumber() != null ? sf.getNextNumber() : 1L);
            seq.setPaddingSize(sf.getPaddingSize() != null ? sf.getPaddingSize() : 5);

            documentSequenceRepository.save(seq);
        }
    }

    private com.gestiva.platform.company.entity.CompanySettings createDefaultSettings(
            Long tenantId,
            Company company) {

        CompanySettings settings = new CompanySettings();

        settings.setTenantId(tenantId);
        settings.setCompany(company);
        settings.setDefaultVatPct(BigDecimal.valueOf(22.00));
        settings.setDefaultCustomerDueDays(30);
        settings.setDefaultSupplierDueDays(30);

        return companySettingsRepository.save(settings);
    }

    private void ensureDefaultSequences(Long tenantId) {
        Map<String, DocumentSequence> existing = documentSequenceRepository.findByTenantIdOrderByDocumentTypeAsc(tenantId)
                .stream()
                .collect(Collectors.toMap(DocumentSequence::getDocumentType, Function.identity()));

        for (String type : DEFAULT_DOCUMENT_TYPES) {
            if (existing.containsKey(type)) {
                continue;
            }

            DocumentSequence seq = new DocumentSequence();
            seq.setTenantId(tenantId);
            seq.setDocumentType(type);
            seq.setPrefix(defaultPrefix(type));
            seq.setNextNumber(1L);
            seq.setPaddingSize(5);

            documentSequenceRepository.save(seq);
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
            default -> "";
        };
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    private Company getMainCompany(Long tenantId) {
        return companyRepository
                .findByTenantIdAndCode(tenantId, "MAIN")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Company MAIN non trovata per il tenant " + tenantId
                        )
                );
    }

}