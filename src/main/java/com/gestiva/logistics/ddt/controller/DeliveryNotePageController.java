package com.gestiva.logistics.ddt.controller;

import com.gestiva.logistics.ddt.dto.DeliveryNoteSearchRequest;
import com.gestiva.logistics.ddt.web.DeliveryNoteWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/delivery-notes")
public class DeliveryNotePageController {

    private final DeliveryNoteWebService deliveryNoteWebService;
    private final TenantContext tenantContext;

    public DeliveryNotePageController(DeliveryNoteWebService deliveryNoteWebService,
                                      TenantContext tenantContext) {
        this.deliveryNoteWebService = deliveryNoteWebService;
        this.tenantContext = tenantContext;
    }
    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "status", required = false) String status,
                       @RequestParam(name = "dateFrom", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateFrom,
                       @RequestParam(name = "dateTo", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateTo,
                       Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        var resultPage = deliveryNoteWebService.findPage(tenantId, page, size, q, status, dateFrom, dateTo);
        model.addAttribute("deliveryNotesPage", resultPage);
        model.addAttribute("page", resultPage);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "deliveryNotes");
        return "ddt/delivery-note-list";
    }
}