package com.gestiva.settings.company.controller;

import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.service.CompanyManagementService;
import com.gestiva.security.usercontext.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import com.gestiva.settings.company.web.CompanyCreateForm;
import com.gestiva.settings.company.web.CompanyEditForm;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyManagementPageControllerTest {

    private static final Long TENANT_ID = 999L;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private CompanyManagementService companyManagementService;

    @Mock
    private Model model;

    @InjectMocks
    private CompanyManagementPageController controller;

    @Test
    void list_shouldUseAuthenticatedTenant() {

        Company company = new Company();
        company.setTenantId(TENANT_ID);
        company.setCode("TEST01");
        company.setLegalName("Società di prova");

        List<Company> companies = List.of(company);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        when(companyManagementService.listCompanies(TENANT_ID))
                .thenReturn(companies);

        String view = controller.list(model);

        assertEquals(
                "settings/company/company-list",
                view
        );

        verify(companyManagementService)
                .listCompanies(TENANT_ID);

        verify(model).addAttribute(
                "companies", companies
        );

        verify(model).addAttribute(
                "activeMenu", "settings"
        );

        verifyNoMoreInteractions(companyManagementService);
    }

    @Test
    void create_shouldUseAuthenticatedTenant() {

        CompanyCreateForm form = new CompanyCreateForm();
        form.setCode("TEST02");
        form.setLegalName("Seconda società di prova");

        BindingResult bindingResult = mock(BindingResult.class);
        RedirectAttributes redirectAttributes =
                mock(RedirectAttributes.class);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        String view = controller.create(
                form,
                bindingResult,
                model,
                redirectAttributes
        );

        assertEquals("redirect:/settings/companies", view);

        verify(companyManagementService).createCompany(
                TENANT_ID,
                "TEST02",
                "Seconda società di prova"
        );

        verify(redirectAttributes).addFlashAttribute(
                "successMessage",
                "Società creata e inizializzata correttamente."
        );

        verifyNoMoreInteractions(companyManagementService);
    }


    @Test
    void create_shouldNotSaveWhenValidationFails() {

        CompanyCreateForm form = new CompanyCreateForm();
        form.setCode("");
        form.setLegalName("");

        BindingResult bindingResult = mock(BindingResult.class);
        RedirectAttributes redirectAttributes =
                mock(RedirectAttributes.class);

        when(bindingResult.hasErrors()).thenReturn(true);

        String view = controller.create(
                form,
                bindingResult,
                model,
                redirectAttributes
        );

        assertEquals(
                "settings/company/company-create",
                view
        );

        verify(model).addAttribute(
                "activeMenu", "settings"
        );

        verifyNoInteractions(
                tenantContext,
                companyManagementService
        );
    }


    @Test
    void editCompany_shouldLoadCompanyFromAuthenticatedTenant() {

        Long companyId = 500L;

        Company company = new Company();
        company.setTenantId(TENANT_ID);
        company.setId(companyId);
        company.setCode("TEST01");
        company.setLegalName("Società esistente");
        company.setActive(true);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        when(companyManagementService.getCompany(
                TENANT_ID, companyId
        )).thenReturn(company);

        String view = controller.editCompany(companyId, model);

        assertEquals("settings/company/company-edit", view);

        verify(companyManagementService).getCompany(
                TENANT_ID, companyId
        );

        verify(model).addAttribute("company", company);

        org.mockito.ArgumentCaptor<CompanyEditForm> captor =
                org.mockito.ArgumentCaptor.forClass(
                        CompanyEditForm.class
                );

        verify(model).addAttribute(
                eq("companyEditForm"),
                captor.capture()
        );

        CompanyEditForm form = captor.getValue();

        assertEquals("Società esistente", form.getLegalName());
        assertTrue(form.isActive());

        verify(model).addAttribute("activeMenu", "settings");
        verifyNoMoreInteractions(companyManagementService);
    }


    @Test
    void updateCompany_shouldUseAuthenticatedTenant() {

        Long companyId = 500L;

        Company company = new Company();
        company.setTenantId(TENANT_ID);
        company.setId(companyId);
        company.setCode("TEST01");
        company.setLegalName("Società originale");
        company.setActive(true);

        CompanyEditForm form = new CompanyEditForm();
        form.setLegalName("Società aggiornata");
        form.setActive(false);

        BindingResult bindingResult = mock(BindingResult.class);
        RedirectAttributes redirectAttributes =
                mock(RedirectAttributes.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        when(companyManagementService.getCompany(
                TENANT_ID, companyId
        )).thenReturn(company);

        when(bindingResult.hasErrors()).thenReturn(false);

        String view = controller.updateCompany(
                companyId,
                form,
                bindingResult,
                model,
                redirectAttributes
        );

        assertEquals("redirect:/settings/companies", view);

        verify(companyManagementService).getCompany(
                TENANT_ID, companyId
        );

        verify(companyManagementService).updateCompany(
                TENANT_ID,
                companyId,
                "Società aggiornata",
                false
        );

        verify(redirectAttributes).addFlashAttribute(
                "successMessage",
                "Società aggiornata correttamente."
        );

        verifyNoMoreInteractions(companyManagementService);
    }


    @Test
    void updateCompany_shouldNotSaveWhenValidationFails() {

        Long companyId = 500L;

        Company company = new Company();
        company.setTenantId(TENANT_ID);
        company.setId(companyId);
        company.setCode("TEST01");
        company.setLegalName("Società originale");
        company.setActive(true);

        CompanyEditForm form = new CompanyEditForm();
        form.setLegalName("");
        form.setActive(false);

        BindingResult bindingResult = mock(BindingResult.class);
        RedirectAttributes redirectAttributes =
                mock(RedirectAttributes.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        when(companyManagementService.getCompany(
                TENANT_ID, companyId
        )).thenReturn(company);

        when(bindingResult.hasErrors()).thenReturn(true);

        String view = controller.updateCompany(
                companyId,
                form,
                bindingResult,
                model,
                redirectAttributes
        );

        assertEquals("settings/company/company-edit", view);

        verify(companyManagementService).getCompany(
                TENANT_ID, companyId
        );

        verify(companyManagementService, never()).updateCompany(
                anyLong(),
                anyLong(),
                anyString(),
                anyBoolean()
        );

        verify(model).addAttribute("company", company);
        verify(model).addAttribute("activeMenu", "settings");

        verifyNoInteractions(redirectAttributes);
        verifyNoMoreInteractions(companyManagementService);
    }

}
