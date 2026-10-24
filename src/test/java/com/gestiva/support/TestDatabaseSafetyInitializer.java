package com.gestiva.support;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.URI;

public class TestDatabaseSafetyInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {

        Environment environment = context.getEnvironment();

        String url = environment.getProperty("spring.datasource.url");

        String username = environment.getProperty(
                "spring.datasource.username"
        );

        if (!"gestiva_test_runner".equals(username)) {
            throw new IllegalStateException(
                    "SICUREZZA TEST: utente MySQL non autorizzato."
            );
        }


        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                    "SICUREZZA TEST: URL database assente."
            );
        }

        try {
            if (!url.startsWith("jdbc:mysql://")) {
                throw new IllegalArgumentException(
                        "Protocollo JDBC non autorizzato"
                );
            }

            URI uri = URI.create(url.substring(5));

            String host = uri.getHost();
            int port = uri.getPort();
            String database = uri.getPath();

            if (!"localhost".equals(host)
                    || port != 3306
                    || !"/gestiva_test".equals(database)
                    || uri.getUserInfo() != null
                    || uri.getFragment() != null) {

                throw new IllegalArgumentException(
                        "Host, porta o database non autorizzati"
                );
            }

            String query = uri.getRawQuery();

            if (query != null && !query.isBlank()) {
                String expected =
                        "useSSL=false"
                        + "&allowPublicKeyRetrieval=true"
                        + "&serverTimezone=Europe/Rome";

                if (!expected.equals(query)) {
                    throw new IllegalArgumentException(
                            "Parametri JDBC non autorizzati"
                    );
                }
            }

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "SICUREZZA TEST: connessione MySQL non autorizzata.",
                    ex
            );
        }

        System.out.println(
                "SICUREZZA TEST: connessione localhost:3306/gestiva_test verificata."
        );
    }
}
