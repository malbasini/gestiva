package com.gestiva.help.service;

public interface OpenAiHelpService {

    String askAssistant(String subscriptionPlan,
                        String roleCode,
                        boolean tenantActive,
                        String currentPage,
                        String userMessage);
}