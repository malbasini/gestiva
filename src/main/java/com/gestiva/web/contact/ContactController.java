package com.gestiva.web.contact;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public String contactPage(Model model) {
        if (!model.containsAttribute("contactForm")) {
            model.addAttribute("contactForm", new ContactForm());
        }
        return "public/contact";
    }

    @PostMapping
    public String sendContact(@Valid @ModelAttribute("contactForm") ContactForm form,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "public/contact";
        }

        contactService.send(form);
        model.addAttribute("successMessage", "Messaggio inviato correttamente.");
        model.addAttribute("contactForm", new ContactForm());
        return "public/contact";
    }
}
