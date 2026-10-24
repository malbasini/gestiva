package com.gestiva.support;

import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestDatabaseSafetyInitializerTest {

    private final TestDatabaseSafetyInitializer initializer =
            new TestDatabaseSafetyInitializer();

    private void verifyAccepted(String url) {
        ConfigurableApplicationContext context =
                mock(ConfigurableApplicationContext.class);

        ConfigurableEnvironment environment =
                mock(ConfigurableEnvironment.class);

        when(context.getEnvironment()).thenReturn(environment);

        when(environment.getProperty("spring.datasource.url"))
                .thenReturn(url);

        when(environment.getProperty("spring.datasource.username"))
                .thenReturn("gestiva_test_runner");

        assertDoesNotThrow(
                () -> initializer.initialize(context)
        );
    }

    private void verifyRejected(String url) {
        ConfigurableApplicationContext context =
                mock(ConfigurableApplicationContext.class);

        ConfigurableEnvironment environment =
                mock(ConfigurableEnvironment.class);

        when(context.getEnvironment()).thenReturn(environment);

        when(environment.getProperty("spring.datasource.url"))
                .thenReturn(url);

        when(environment.getProperty("spring.datasource.username"))
                .thenReturn("gestiva_test_runner");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> initializer.initialize(context)
        );

        assertTrue(
                exception.getMessage().contains(
                        "connessione MySQL non autorizzata"
                )
        );
    }

    @Test
    void shouldAcceptGestivaTestDatabase() {
        verifyAccepted(
                "jdbc:mysql://localhost:3306/gestiva_test"
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=Europe/Rome"
        );
    }

    @Test
    void shouldRejectProductionDatabase() {
        verifyRejected(
                "jdbc:mysql://localhost:3306/gestiva"
        );
    }

    @Test
    void shouldRejectInvalidDatabaseUrl() {
        verifyRejected("invalid-url");
    }

    @Test
    void shouldRejectRemoteHost() {
        verifyRejected(
                "jdbc:mysql://example.com:3306/gestiva_test"
        );
    }

    @Test
    void shouldRejectDifferentPort() {
        verifyRejected(
                "jdbc:mysql://localhost:3307/gestiva_test"
        );
    }

    @Test
    void shouldRejectUnauthorizedJdbcParameters() {
        verifyRejected(
                "jdbc:mysql://localhost:3306/gestiva_test"
                + "?allowMultiQueries=true"
        );
    }

    @Test
    void shouldRejectProductionUrlBeforeContextInitialization() {

        ConfigurableApplicationContext context =
                mock(ConfigurableApplicationContext.class);

        ConfigurableEnvironment environment =
                mock(ConfigurableEnvironment.class);

        when(context.getEnvironment()).thenReturn(environment);

        when(environment.getProperty("spring.datasource.url"))
                .thenReturn(
                        "jdbc:mysql://localhost:3306/gestiva"
                );

        when(environment.getProperty("spring.datasource.username"))
                .thenReturn("gestiva_test_runner");

        verifyNoInteractions(context);

        assertThrows(
                IllegalStateException.class,
                () -> initializer.initialize(context)
        );

        verify(context).getEnvironment();

        verify(environment).getProperty(
                "spring.datasource.url"
        );

        verify(environment).getProperty(
                "spring.datasource.username"
        );

        verifyNoMoreInteractions(context, environment);
    }

    @Test
    void shouldRejectRootDatabaseUser() {

        ConfigurableApplicationContext context =
                mock(ConfigurableApplicationContext.class);

        ConfigurableEnvironment environment =
                mock(ConfigurableEnvironment.class);

        when(context.getEnvironment()).thenReturn(environment);

        when(environment.getProperty("spring.datasource.url"))
                .thenReturn(
                        "jdbc:mysql://localhost:3306/gestiva_test"
                );

        when(environment.getProperty("spring.datasource.username"))
                .thenReturn("root");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> initializer.initialize(context)
        );

        assertTrue(
                exception.getMessage().contains(
                        "utente MySQL non autorizzato"
                )
        );
    }

    @Test
    void shouldRejectMissingDatabase() {
        verifyRejected(
                "jdbc:mysql://localhost:3306/"
        );
    }
}
