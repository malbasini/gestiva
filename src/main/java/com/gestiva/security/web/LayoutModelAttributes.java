package com.gestiva.security.web;

import com.gestiva.security.service.CurrentUserService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class LayoutModelAttributes {

    private final CurrentUserService currentUserService;

    public LayoutModelAttributes(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("currentUser", currentUserService.getCurrentUserView());
    }
}