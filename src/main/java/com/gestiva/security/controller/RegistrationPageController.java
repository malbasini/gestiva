package com.gestiva.security.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.service.RegistrationService;
import com.gestiva.security.web.RegistrationForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationPageController {

    private final RegistrationService registrationService;

    public RegistrationPageController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            var result = registrationService.register(form);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Registrazione completata. Ora puoi accedere con tenant '" + result.getTenantSlug() + "'."
            );
            return "redirect:/login";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
    }
}