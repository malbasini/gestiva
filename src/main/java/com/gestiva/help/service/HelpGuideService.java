package com.gestiva.help.service;

import org.springframework.stereotype.Service;

@Service
public class HelpGuideService {

    public String askGuide(String subscriptionPlan,
                           boolean tenantActive,
                           String userMessage) {

        String question = userMessage == null ? "" : userMessage.trim().toLowerCase();

        if (!tenantActive) {
            return "Il tenant non risulta ancora attivo. In questa fase puoi consultare la pagina prezzi e completare l’attivazione del piano.";
        }

        if (question.contains("contabilità") && "STARTER".equalsIgnoreCase(subscriptionPlan)) {
            return "Il modulo Contabilità non è incluso nel piano Starter. Nel piano Starter restano disponibili il ciclo attivo e il ciclo passivo.";
        }

        if (question.contains("ddt")) {
            return "Per generare un DDT, apri un ordine confermato nel ciclo attivo e utilizza l’azione di creazione DDT, se disponibile per il tuo ruolo.";
        }

        if (question.contains("incasso")) {
            return "Per registrare un incasso, apri lo scadenzario o il documento collegato e usa l’azione di registrazione del pagamento. Se hai già generato la scrittura contabile, verifica anche l’aggiornamento della prima nota.";
        }

        if (question.contains("cliente")) {
            return "Per creare un cliente, entra nel ciclo attivo e apri la sezione Clienti. Da lì puoi inserire una nuova anagrafica e salvarla.";
        }

        if (question.contains("fornitore")) {
            return "Per creare un fornitore, entra nel ciclo passivo e apri la sezione Fornitori. Da lì puoi inserire una nuova anagrafica.";
        }
        return "";
    }
}
