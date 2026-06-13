package com.gestiva.help.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("dev")
@Service
public class MockOpenAiHelpService implements OpenAiHelpService {

    @Override
    public String askAssistant(String subscriptionPlan,
                               String roleCode,
                               boolean tenantActive,
                               String currentPage,
                               String userMessage) {

        return "Sto passando la richiesta al motore AI.\n\n" +
                "Domanda ricevuta: " + userMessage + "\n\n" +
                "Questa è una risposta mock temporanea. " +
                "Quando collegheremo OpenAI, qui arriverà una spiegazione più avanzata e contestuale.";
    }
}