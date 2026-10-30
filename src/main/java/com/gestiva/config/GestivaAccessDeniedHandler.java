package com.gestiva.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.Locale;

public class GestivaAccessDeniedHandler implements AccessDeniedHandler {

    private static final String SAFE_MESSAGE =
            "Non hai i permessi necessari per accedere alla risorsa richiesta.";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {

        if (requiresStructuredResponse(request)) {
            writeProblemDetail(response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        request.getRequestDispatcher("/403").forward(
                request,
                response
        );
    }

    private boolean requiresStructuredResponse(
            HttpServletRequest request
    ) {

        String path = request.getServletPath();

        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
        }

        if (path == null) {
            path = "";
        }

        String normalizedPath = path.toLowerCase(Locale.ROOT);

        if (normalizedPath.equals("/api")
                || normalizedPath.startsWith("/api/")) {
            return true;
        }

        if (normalizedPath.endsWith("/export.csv")
                || normalizedPath.endsWith("/export.xlsx")
                || normalizedPath.endsWith("/pdf")
                || normalizedPath.contains("/pdf/")) {
            return true;
        }

        if (normalizedPath.equals("/help/chat")
                || normalizedPath.startsWith("/billing/paypal/")) {
            return true;
        }

        String requestedWith =
                request.getHeader("X-Requested-With");

        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }

        String accept = request.getHeader("Accept");

        if (accept != null) {
            String normalizedAccept =
                    accept.toLowerCase(Locale.ROOT);

            if (normalizedAccept.contains("application/json")
                    || normalizedAccept.contains(
                            "application/problem+json"
                    )) {
                return true;
            }

            if (normalizedAccept.contains("application/pdf")
                    || normalizedAccept.contains(
                            "application/octet-stream"
                    )) {
                return true;
            }
        }

        return false;
    }

    private void writeProblemDetail(
            HttpServletResponse response
    ) throws IOException {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.FORBIDDEN);

        problem.setTitle("Accesso negato");
        problem.setDetail(SAFE_MESSAGE);

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        response.setContentType(
                MediaType.APPLICATION_PROBLEM_JSON_VALUE
        );

        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getOutputStream(),
                problem
        );
    }
}
