package com.gestiva.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import com.gestiva.security.auth.CustomUserDetailsService;
import com.gestiva.security.auth.TenantUsernamePasswordAuthenticationFilter;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Test HTTP 403 con Tomcat Embedded.
 *
 * Il container è isolato dall'applicazione Gestiva.
 * Non vengono caricati repository, datasource o servizi aziendali.
 *
 * Il test rimane disabilitato fino al completamento
 * della configurazione Spring Security.
 */
class GestivaAccessDeniedEmbeddedTomcatTest {

    @Test
    void shouldReturnHtml403ThroughRealServletForward() throws Exception {

        Path baseDirectory =
                Files.createTempDirectory("gestiva-tomcat-403-");

        Tomcat tomcat = new Tomcat();
        Throwable failure = null;

        try {
            tomcat.setBaseDir(baseDirectory.toString());
            tomcat.setPort(0);

            Context context = tomcat.addContext(
                    "",
                    baseDirectory.toString()
            );

            HttpServlet healthServlet = new HttpServlet() {

                @Override
                protected void doGet(
                        HttpServletRequest request,
                        HttpServletResponse response
                ) throws IOException {

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().write("TEST_HTTP_OK");
                }
            };

            Tomcat.addServlet(
                    context,
                    "testHealthServlet",
                    healthServlet
            );

            context.addServletMappingDecoded(
                    "/test-health",
                    "testHealthServlet"
            );

            HttpServlet forbiddenServlet = new HttpServlet() {

                @Override
                protected void doGet(
                        HttpServletRequest request,
                        HttpServletResponse response
                ) throws IOException {

                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().write(
                            "<!DOCTYPE html>"
                            + "<html lang=\"it\">"
                            + "<head><title>Accesso negato</title></head>"
                            + "<body><h1>Accesso negato</h1></body>"
                            + "</html>"
                    );
                }
            };

            Tomcat.addServlet(
                    context,
                    "testForbiddenServlet",
                    forbiddenServlet
            );

            context.addServletMappingDecoded(
                    "/403",
                    "testForbiddenServlet"
            );

            AtomicBoolean protectedServletReached =
                    new AtomicBoolean(false);

            HttpServlet protectedServlet = new HttpServlet() {

                @Override
                protected void doGet(
                        HttpServletRequest request,
                        HttpServletResponse response
                ) throws IOException {

                    protectedServletReached.set(true);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("ACCESSO_NON_AUTORIZZATO");
                }
            };

            Tomcat.addServlet(
                    context,
                    "testProtectedServlet",
                    protectedServlet
            );

            context.addServletMappingDecoded(
                    "/settings/test-access-denied",
                    "testProtectedServlet"
            );

            CustomUserDetailsService userDetailsService =
                    mock(CustomUserDetailsService.class);

            DataSource dataSource = mock(DataSource.class);

            AuthenticationProvider authenticationProvider =
                    mock(AuthenticationProvider.class);

            RememberMeServices rememberMeServices =
                    mock(RememberMeServices.class);

            SecurityConfig securityConfig =
                    new SecurityConfig(userDetailsService, dataSource);

            TenantUsernamePasswordAuthenticationFilter loginFilter =
                    securityConfig.tenantUsernamePasswordAuthenticationFilter(
                            authentication -> authentication,
                            rememberMeServices
                    );

            ObjectPostProcessor<Object> postProcessor =
                    new ObjectPostProcessor<>() {
                        @Override
                        public <O> O postProcess(O object) {
                            return object;
                        }
                    };

            AuthenticationManagerBuilder authenticationBuilder =
                    new AuthenticationManagerBuilder(postProcessor);

            StaticApplicationContext applicationContext =
                    new StaticApplicationContext();

            applicationContext.refresh();

            HashMap<Class<?>, Object> sharedObjects =
                    new HashMap<>();

            sharedObjects.put(
                    org.springframework.context.ApplicationContext.class,
                    applicationContext
            );

            HttpSecurity http = new HttpSecurity(
                    postProcessor,
                    authenticationBuilder,
                    sharedObjects
            );

            SecurityContextRepository securityContextRepository =
                    new HttpSessionSecurityContextRepository();

            http.securityContext(securityContext ->
                    securityContext.securityContextRepository(
                            securityContextRepository
                    )
            );

            SecurityFilterChain securityFilterChain =
                    securityConfig.securityFilterChain(
                            http,
                            loginFilter,
                            authenticationProvider
                    );

            FilterChainProxy proxy =
                    new FilterChainProxy(securityFilterChain);

            Filter testAuthenticationFilter = new Filter() {

                @Override
                public void doFilter(
                        ServletRequest servletRequest,
                        ServletResponse servletResponse,
                        FilterChain chain
                ) throws IOException, ServletException {

                    HttpServletRequest request =
                            (HttpServletRequest) servletRequest;

                    HttpServletResponse response =
                            (HttpServletResponse) servletResponse;

                    TestingAuthenticationToken authentication =
                            new TestingAuthenticationToken(
                                    "test-sales-user",
                                    "not-a-real-password",
                                    "ROLE_SALES"
                            );

                    authentication.setAuthenticated(true);

                    SecurityContext securityContext =
                            SecurityContextHolder.createEmptyContext();

                    securityContext.setAuthentication(authentication);

                    securityContextRepository.saveContext(
                            securityContext,
                            request,
                            response
                    );

                    chain.doFilter(request, response);
                }
            };

            FilterDef authenticationFilterDefinition = new FilterDef();
            authenticationFilterDefinition.setFilterName(
                    "gestivaTestAuthenticationFilter"
            );
            authenticationFilterDefinition.setFilter(
                    testAuthenticationFilter
            );

            context.addFilterDef(authenticationFilterDefinition);

            FilterMap authenticationFilterMapping = new FilterMap();
            authenticationFilterMapping.setFilterName(
                    "gestivaTestAuthenticationFilter"
            );
            authenticationFilterMapping.addURLPattern("/*");
            authenticationFilterMapping.setDispatcher("REQUEST");

            context.addFilterMap(authenticationFilterMapping);

            FilterDef filterDefinition = new FilterDef();
            filterDefinition.setFilterName("gestivaSecurityTestFilter");
            filterDefinition.setFilter(proxy);

            context.addFilterDef(filterDefinition);

            FilterMap filterMapping = new FilterMap();
            filterMapping.setFilterName("gestivaSecurityTestFilter");
            filterMapping.addURLPattern("/*");
            filterMapping.setDispatcher("REQUEST");

            context.addFilterMap(filterMapping);

            tomcat.getConnector();
            tomcat.start();

            int assignedPort =
                    tomcat.getConnector().getLocalPort();

            if (assignedPort <= 0) {
                throw new IllegalStateException(
                        "Tomcat non ha assegnato una porta HTTP valida."
                );
            }

            URI uri = URI.create(
                    "http://127.0.0.1:"
                    + assignedPort
                    + "/settings/test-access-denied"
            );

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Accept", "text/html")
                    .GET()
                    .build();

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                assertEquals(
                        403,
                        response.statusCode(),
                        "L'utente SALES deve ricevere HTTP 403"
                );

                String contentType = response.headers()
                        .firstValue("Content-Type")
                        .orElse("");

                assertTrue(
                        contentType.toLowerCase().contains("text/html"),
                        "La risposta deve essere HTML"
                );

                assertTrue(
                        response.body().contains("Accesso negato"),
                        "La pagina deve indicare l'accesso negato"
                );

                assertFalse(
                        response.body().contains("ACCESSO_NON_AUTORIZZATO"),
                        "Il servlet protetto non deve produrre la risposta"
                );

                assertFalse(
                        protectedServletReached.get(),
                        "Il servlet amministrativo non deve essere eseguito"
                );

                verify(dataSource, never()).getConnection();
            }

        } catch (Exception | Error ex) {
            failure = ex;
            throw ex;

        } finally {
            try {
                tomcat.stop();
            } catch (Exception | Error ex) {
                if (failure != null) {
                    failure.addSuppressed(ex);
                } else {
                    failure = ex;
                }
            }

            try {
                tomcat.destroy();
            } catch (Exception | Error ex) {
                if (failure != null) {
                    failure.addSuppressed(ex);
                } else {
                    failure = ex;
                }
            }

            try {
                deleteTemporaryDirectory(baseDirectory);
            } catch (Exception | Error ex) {
                if (failure != null) {
                    failure.addSuppressed(ex);
                } else {
                    failure = ex;
                }
            }

            if (failure != null) {
                if (failure instanceof Exception exception) {
                    throw exception;
                }
                throw (Error) failure;
            }
        }
    }

    private static void deleteTemporaryDirectory(
            Path directory
    ) throws IOException {

        if (!Files.exists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths
                    .sorted(Comparator.reverseOrder())
                    .toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
