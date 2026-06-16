package com.gestiva.web.publicsite;

import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicHomeController {
    @GetMapping("/")
    public String home() {
        return "public/home";
    }
}