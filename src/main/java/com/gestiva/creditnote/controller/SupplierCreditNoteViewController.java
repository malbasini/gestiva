package com.gestiva.creditnote.controller;

import com.gestiva.creditnote.service.SupplierCreditNoteDetailService;
import com.gestiva.security.usercontext.TenantContext;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/supplier-credit-notes")
public class SupplierCreditNoteViewController {

    private final TenantContext tenantContext;
    private final SupplierCreditNoteDetailService detailService;

    public SupplierCreditNoteViewController(
            TenantContext tenantContext,
            SupplierCreditNoteDetailService detailService) {
        this.tenantContext = tenantContext;
        this.detailService = detailService;
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        var detail = detailService.getDetail(tenantId, id);
        var note = detail.note();

        boolean canRegister = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(authority.getAuthority())
                                || "ROLE_ACCOUNTING".equals(
                                        authority.getAuthority()))
                && "DRAFT".equals(note.getStatus());

        model.addAttribute("creditNote", note);
        model.addAttribute("creditNoteLines", detail.lines());
        model.addAttribute("canRegisterCreditNote", canRegister);
        model.addAttribute("activeMenu", "supplierInvoices");

        return "creditnote/supplier-credit-note-detail";
    }
}
