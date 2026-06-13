package com.gestiva.help.service;

import org.springframework.stereotype.Service;

@Service
public class HelpGuideService {

    public HelpGuideResult askGuide(Long tenantId,
                                    String subscriptionPlan,
                                    String roleCode,
                                    boolean tenantActive,
                                    String currentPage,
                                    String userMessage) {

        String question = normalize(userMessage);

        if (!tenantActive) {
            return HelpGuideResult.handled(
                    "Il tenant non risulta ancora attivo. In questa fase puoi consultare la pagina prezzi e completare l’attivazione del piano."
            );
        }

        if (containsAny(question, "magazzino", "fifo", "lifo", "costo medio", "valorizzazione")) {
            if ("STARTER".equalsIgnoreCase(subscriptionPlan)) {
                return HelpGuideResult.handled(
                        "Il modulo Magazzino non è incluso nel piano Starter. Per utilizzare funzioni come rettifiche inventario, giacenza valorizzata e costo del venduto è necessario il piano Professional."
                );
            }
            return HelpGuideResult.notHandled();
        }

        if (containsAny(question, "contabilità", "prima nota", "partita doppia", "liquidazione iva")) {
            if ("STARTER".equalsIgnoreCase(subscriptionPlan)) {
                return HelpGuideResult.handled(
                        "Il modulo Contabilità non è incluso nel piano Starter. Nel piano Starter restano disponibili il ciclo attivo e il ciclo passivo."
                );
            }
            return HelpGuideResult.notHandled();
        }

        if (containsAny(question, "cliente", "creare cliente", "nuovo cliente")) {
            return HelpGuideResult.handled(
                    "Per creare un cliente, entra nel ciclo attivo e apri la sezione Clienti. Da lì puoi inserire una nuova anagrafica e salvarla."
            );
        }

        if (containsAny(question, "fornitore", "creare fornitore", "nuovo fornitore")) {
            return HelpGuideResult.handled(
                    "Per creare un fornitore, entra nel ciclo passivo e apri la sezione Fornitori. Da lì puoi inserire una nuova anagrafica e salvarla."
            );
        }

        if (containsAny(question, "ordine", "creare ordine", "generare ordine")) {
            return HelpGuideResult.handled(
                    "Per creare un ordine, entra nel ciclo attivo e apri la sezione Ordini. Da lì puoi inserire un nuovo ordine, compilare i dati principali e aggiungere le righe prima del salvataggio."
            );
        }

        if (containsAny(question, "ddt", "documento di trasporto")) {
            return HelpGuideResult.handled(
                    "Per generare un DDT, apri un ordine confermato nel ciclo attivo e utilizza l’azione di creazione DDT, se disponibile per il tuo ruolo."
            );
        }

        if (containsAny(question, "incasso", "registrare incasso", "registrazione incasso")) {
            return HelpGuideResult.handled(
                    "Per registrare un incasso, apri lo scadenzario o il documento collegato e usa l’azione di registrazione del pagamento."
            );
        }

        if (containsAny(question, "menu", "menù", "non vedo")) {
            return HelpGuideResult.handled(
                    "Se non vedi un menù o una funzione, il motivo può dipendere dal tuo ruolo utente oppure dal piano attivo del tenant."
            );
        }

        if (containsAny(question, "piano", "starter", "professional", "funzioni incluse")) {
            return HelpGuideResult.handled(
                    "Il piano Starter include il ciclo attivo e il ciclo passivo. Il piano Professional include anche Magazzino e Contabilità."
            );
        }

        return HelpGuideResult.notHandled();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean containsAny(String text, String... tokens) {
        if (text == null || text.isBlank()) {
            return false;
        }

        for (String token : tokens) {
            if (token != null && !token.isBlank() && text.contains(token.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}