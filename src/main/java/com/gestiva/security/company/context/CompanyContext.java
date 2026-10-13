package com.gestiva.security.company.context;

import com.gestiva.platform.company.entity.Company;
import com.gestiva.security.company.service.CompanyAccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CompanyContext {

    private static final String SESSION_ATTRIBUTE =
            "GESTIVA_ACTIVE_COMPANY_ID";

    private final HttpServletRequest request;
    private final CompanyAccessService companyAccessService;

    public CompanyContext(
            HttpServletRequest request,
            CompanyAccessService companyAccessService) {

        this.request = request;
        this.companyAccessService = companyAccessService;
    }

    public Long getCurrentCompanyIdOrNull() {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(SESSION_ATTRIBUTE);

        if (!(value instanceof Long companyId)) {
            return null;
        }

        // Verifica sempre che l'utente possa ancora
        // accedere alla società selezionata.
        companyAccessService.requireAccess(companyId);

        return companyId;
    }

    public Long requireCurrentCompanyId() {

        Long companyId = getCurrentCompanyIdOrNull();

        if (companyId == null) {
            throw new IllegalStateException(
                    "Nessuna società attiva selezionata"
            );
        }

        return companyId;
    }

    public Company selectCompany(Long companyId) {

        // Verifica l'autorizzazione prima di modificare la sessione.
        Company company = companyAccessService.requireAccess(companyId);

        HttpSession session = request.getSession(true);

        session.setAttribute(SESSION_ATTRIBUTE, company.getId());

        return company;
    }

    public void clear() {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.removeAttribute(SESSION_ATTRIBUTE);
        }
    }

    public Long resolveCurrentCompanyId() {

        // Recupera la società già selezionata nella sessione.
        Long currentCompanyId = getCurrentCompanyIdOrNull();

        if (currentCompanyId != null) {
            return currentCompanyId;
        }

        // Recupera le società accessibili all'utente autenticato.
        List<Company> accessibleCompanies =
                companyAccessService.findAccessibleCompanies();

        // Se l'utente ha una sola società accessibile,
        // la selezioniamo automaticamente.
        if (accessibleCompanies.size() == 1) {

            Company company = accessibleCompanies.get(0);

            selectCompany(company.getId());

            return company.getId();
        }

        // Nessuna società oppure più società:
        // sarà necessaria una selezione esplicita.
        return null;
    }
}