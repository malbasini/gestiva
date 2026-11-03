package com.gestiva.admin.users.controller;

import com.gestiva.admin.users.service.AdminUserService;
import com.gestiva.admin.users.web.AdminUserCompanyOptionView;
import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.admin.users.web.AdminUserPageView;
import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AdminUserCompanyTemplateIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long USER = 100L;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AdminUserService service;

    private UsernamePasswordAuthenticationToken adminAuthentication() {

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        AuthenticatedUser principal = new AuthenticatedUser(
                1L,
                TENANT,
                "tenant-test",
                "admin@example.invalid",
                "test-password",
                true,
                authorities
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );
    }

    private AdminUserPageView pageWithCompanies() {

        AdminUserPageView page = new AdminUserPageView();

        page.setAvailableRoles(List.of(
                "ROLE_SALES",
                "ROLE_WAREHOUSE"
        ));

        page.setAvailableCompanies(List.of(
                new AdminUserCompanyOptionView(
                        10L, "Società Alfa"
                ),
                new AdminUserCompanyOptionView(
                        20L, "Società Beta"
                )
        ));

        return page;
    }

    private String renderEdit(
            AdminUserPageView page,
            AdminUserEditForm form) throws Exception {

        when(service.buildPage(TENANT)).thenReturn(page);

        when(service.loadEditForm(TENANT, USER))
                .thenReturn(form);

        MvcResult result = mvc.perform(
                    get("/admin/users/{id}/edit", USER)
                        .with(authentication(
                                adminAuthentication()
                        ))
                        .header("X-Tenant-Id", TENANT)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "admin/users/user-list"
                ))
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private boolean isChecked(
            String html,
            String name,
            String value) {

        // Riconosce l'attributo checked indipendentemente
        // dall'ordine degli attributi HTML.
        Pattern input = Pattern.compile(
                "<input\\b[^>]*>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        var matches = input.matcher(html);

        while (matches.find()) {

            String tag = matches.group();

            if (tag.matches(
                    "(?s).*\\bname\\s*=\\s*\"" +
                    Pattern.quote(name) + "\".*"
            ) && tag.matches(
                    "(?s).*\\bvalue\\s*=\\s*\"" +
                    Pattern.quote(value) + "\".*"
            )) {
                return Pattern.compile(
                        "\\bchecked(?:\\s|=|/|>)",
                        Pattern.CASE_INSENSITIVE
                ).matcher(tag).find();
            }
        }

        throw new AssertionError(
                "Checkbox non trovata: " + name + "=" + value
        );
    }

    @Test
    void editShouldRenderSelectedCompaniesAndRoles()
            throws Exception {

        AdminUserEditForm form = new AdminUserEditForm();

        form.setId(USER);
        form.setFirstName("Mario");
        form.setLastName("Rossi");
        form.setEmail("mario@example.invalid");
        form.setActive(true);
        form.setCompanyIds(List.of(10L));
        form.setRoles(List.of("ROLE_SALES"));

        String html = renderEdit(
                pageWithCompanies(),
                form
        );

        assertTrue(html.contains("Società autorizzate"));
        assertTrue(html.contains("Società Alfa"));
        assertTrue(html.contains("Società Beta"));

        assertTrue(isChecked(html, "companyIds", "10"));
        assertFalse(isChecked(html, "companyIds", "20"));

        assertTrue(isChecked(html, "roles", "ROLE_SALES"));
        assertFalse(isChecked(html, "roles", "ROLE_WAREHOUSE"));

        System.out.println(
                "THYMELEAF OK: società e ruoli preselezionati."
        );
    }

    @Test
    void emptyCompanyListShouldRenderMessage()
            throws Exception {

        AdminUserPageView page = new AdminUserPageView();

        AdminUserEditForm form = new AdminUserEditForm();
        form.setId(USER);
        form.setFirstName("Mario");
        form.setLastName("Rossi");
        form.setEmail("mario@example.invalid");

        String html = renderEdit(page, form);

        assertTrue(
                html.contains(
                        "Nessuna società attiva disponibile."
                )
        );

        System.out.println(
                "THYMELEAF OK: elenco società vuoto."
        );
    }
}
