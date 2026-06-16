package com.gestiva.price;

import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping()
public class PriceController {

    @GetMapping(value = "/pricing")
    public String page(Model model) {
        model.addAttribute("activeMenu", "price");
        return "public/pricing";

    }
}
