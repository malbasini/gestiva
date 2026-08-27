package com.gestiva.admin.users.controller;

import com.gestiva.admin.users.service.AdminUserService;
import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserPageController {

    private final TenantContext tenantContext;
    private final AdminUserService adminUserService;

    public AdminUserPageController(TenantContext tenantContext,
                                   AdminUserService adminUserService) {
        this.tenantContext = tenantContext;
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public String page(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute("pageView", adminUserService.buildPage(tenantId));

        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new AdminUserEditForm());
        }

        model.addAttribute("activeMenu", "settings");
        return "admin/users/user-list";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("userForm") AdminUserEditForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageView", adminUserService.buildPage(tenantId));
            model.addAttribute("activeMenu", "settings");
            return "admin/users/user-list";
        }

        try {
            adminUserService.createUser(tenantId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Utente creato con successo.");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute("pageView", adminUserService.buildPage(tenantId));
        model.addAttribute("userForm", adminUserService.loadEditForm(tenantId, id));
        model.addAttribute("editMode", true);
        model.addAttribute("activeMenu", "settings");

        return "admin/users/user-list";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("userForm") AdminUserEditForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();
        form.setId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageView", adminUserService.buildPage(tenantId));
            model.addAttribute("editMode", true);
            model.addAttribute("activeMenu", "settings");
            return "admin/users/user-list";
        }

        adminUserService.updateUser(tenantId, form);
        redirectAttributes.addFlashAttribute("successMessage", "Utente aggiornato con successo.");
        return "redirect:/admin/users";
    }
}