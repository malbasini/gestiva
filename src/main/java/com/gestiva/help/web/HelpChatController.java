package com.gestiva.help.web;

import com.gestiva.help.service.HelpGuideResult;
import com.gestiva.help.service.HelpGuideService;
import com.gestiva.help.service.OpenAiHelpService;
import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/help")
public class HelpChatController {

    private final HelpGuideService helpGuideService;
    private final OpenAiHelpService openAiHelpService;
    private final TenantRepository tenantRepository;

    public HelpChatController(HelpGuideService helpGuideService,
                              OpenAiHelpService openAiHelpService,
                              TenantRepository tenantRepository) {
        this.helpGuideService = helpGuideService;
        this.openAiHelpService = openAiHelpService;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping("/chat")
    public ResponseEntity<HelpChatResponse> chat(@RequestBody HelpChatRequest request,
                                                 Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return ResponseEntity.status(401).body(new HelpChatResponse("Utente non autenticato."));
        }

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Tenant non trovato."));

        String roleCode = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_USER");

        HelpGuideResult localResult = helpGuideService.askGuide(
                user.getTenantId(),
                tenant.getSubscriptionPlan(),
                roleCode,
                tenant.isSubscriptionActive(),
                request.getCurrentPage(),
                request.getMessage()
        );

        if (localResult.isHandled()) {
            return ResponseEntity.ok(new HelpChatResponse(localResult.getAnswer()));
        }

        String aiAnswer = openAiHelpService.askAssistant(
                tenant.getSubscriptionPlan(),
                roleCode,
                tenant.isSubscriptionActive(),
                request.getCurrentPage(),
                request.getMessage()
        );

        return ResponseEntity.ok(new HelpChatResponse(aiAnswer));
    }
}