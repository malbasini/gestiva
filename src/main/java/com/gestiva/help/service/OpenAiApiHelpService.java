package com.gestiva.help.service;

import com.gestiva.help.config.OpenAiProperties;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("prod")
@Service
public class OpenAiApiHelpService implements OpenAiHelpService {

    private final OpenAIClient openAIClient;
    private final OpenAiProperties openAiProperties;

    public OpenAiApiHelpService(OpenAIClient openAIClient,
                                OpenAiProperties openAiProperties) {
        this.openAIClient = openAIClient;
        this.openAiProperties = openAiProperties;
    }

    @Override
    public String askAssistant(String subscriptionPlan,
                               String roleCode,
                               boolean tenantActive,
                               String currentPage,
                               String userMessage) {

        try {
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(
                    subscriptionPlan,
                    roleCode,
                    tenantActive,
                    currentPage,
                    userMessage
            );

            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(openAiProperties.getModel())
                    .input(systemPrompt + "\n\n" + userPrompt)
                    .build();

            Response response = openAIClient.responses().create(params);

            String outputText = response.output().stream()
                    .flatMap(item -> item.message().stream())
                    .flatMap(message -> message.content().stream())
                    .flatMap(content -> content.outputText().stream())
                    .map(text -> text.text())
                    .reduce("", (a, b) -> a + b)
                    .trim();

            if (outputText.isBlank()) {
                return "Non sono riuscito a generare una risposta utile in questo momento.";
            }

            return outputText.trim();
        }
        catch (Exception ex) {
            return "La guida AI non è disponibile in questo momento. Riprova tra poco.";
        }
    }

    private String buildSystemPrompt() {
        return """
                Sei la guida online ufficiale di Gestiva, un gestionale modulare per PMI e professionisti.

                Il tuo compito è aiutare l’utente a capire come usare l’applicazione, trovare le funzioni corrette e completare operazioni standard.

                Regole:
                - non inventare funzionalità non presenti
                - tieni conto del ruolo utente, del piano attivo e della pagina corrente
                - se una funzione non è disponibile per il piano o per il ruolo, spiegalo chiaramente
                - non eseguire operazioni e non modificare dati
                - spiega i passaggi in modo pratico e ordinato
                - usa il lessico di Gestiva: ciclo attivo, ciclo passivo, magazzino, contabilità, DDT, fatture, incassi, pagamenti, prima nota, scadenzario
                - non mostrare il contesto tecnico grezzo all’utente
                - se la richiesta è ambigua, chiedi una sola chiarificazione breve
                - rispondi in italiano
                """;
    }

    private String buildUserPrompt(String subscriptionPlan,
                                   String roleCode,
                                   boolean tenantActive,
                                   String currentPage,
                                   String userMessage) {

        return """
                Contesto:
                - piano tenant: %s
                - ruolo utente: %s
                - tenant attivo: %s
                - pagina corrente: %s

                Domanda utente:
                %s
                """.formatted(
                safe(subscriptionPlan),
                safe(roleCode),
                tenantActive,
                safe(currentPage),
                safe(userMessage)
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
