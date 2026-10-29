package com.gestiva.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(
                    new TestRestController(),
                    new TestPageController()
            )
            .setControllerAdvice(
                    new GlobalExceptionHandler(),
                    new HtmlExceptionHandler()
            )
            .build();

    @Test
    void shouldReturnProblemDetailForRestBusinessException()
            throws Exception {

        mockMvc.perform(
                    get("/test/errors/business")
                            .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title").value(
                        "Business error"
                ))
                .andExpect(jsonPath("$.detail").value(
                        "Operazione non consentita"
                ));
    }

    @Test
    void shouldReturnHtmlForWebBusinessException()
            throws Exception {

        mockMvc.perform(
                    get("/test/pages/business")
                            .accept(MediaType.TEXT_HTML)
                )
                .andExpect(status().isBadRequest())
                .andExpect(view().name(
                        "error/business-error"
                ))
                .andExpect(model().attribute(
                        "errorMessage",
                        "Operazione non consentita"
                ));
    }

    @Controller
    static class TestPageController {

        @GetMapping("/test/pages/export-not-found.xlsx")
        public org.springframework.http.ResponseEntity<byte[]>
                exportNotFoundXlsx() {
            throw new NotFoundException(
                    "Dettaglio tecnico interno non pubblico"
            );
        }

        @GetMapping("/test/pages/export-business.csv")
        public org.springframework.http.ResponseEntity<byte[]>
                exportBusinessCsv() {
            throw new BusinessException("Operazione non consentita");
        }

        @GetMapping("/test/pages/business")
        public String businessError() {
            throw new BusinessException(
                    "Operazione non consentita"
            );
        }

        @GetMapping("/test/pages/not-found")
        public String notFoundError() {
            throw new NotFoundException(
                    "Dettaglio tecnico interno non pubblico"
            );
        }

        @GetMapping("/test/pages/internal-error")
        public String internalError() {
            throw new IllegalStateException(
                    "Dettaglio tecnico riservato: errore interno"
            );
        }

        @GetMapping("/test/pages/access-denied")
        public String accessDenied() {
            throw new AccessDeniedException(
                    "Accesso non autorizzato"
            );
        }

        @GetMapping("/test/pages/export.csv")
        public org.springframework.http.ResponseEntity<byte[]> exportCsv() {
            throw new IllegalStateException(
                    "Dettaglio tecnico riservato: errore esportazione CSV"
            );
        }
        
        @GetMapping("/test/pages/export.xlsx")
        public org.springframework.http.ResponseEntity<byte[]> exportXlsx() {
            throw new IllegalStateException(
                    "Dettaglio tecnico riservato: errore esportazione XLSX"
            );
        }
    }

    @Test
    void shouldReturnHtmlForWebNotFoundException()
            throws Exception {

        mockMvc.perform(
                    get("/test/pages/not-found")
                            .accept(MediaType.TEXT_HTML)
                )
                .andExpect(status().isNotFound())
                .andExpect(view().name(
                        "error/business-error"
                ))
                .andExpect(model().attribute(
                        "errorMessage",
                        "La risorsa richiesta non è stata trovata."
                ));
    }

    @Test
    void shouldReturnSafeHtmlForUnexpectedWebException()
            throws Exception {

        mockMvc.perform(
                    get("/test/pages/internal-error")
                            .accept(MediaType.TEXT_HTML)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(view().name(
                        "error/business-error"
                ))
                .andExpect(model().attribute(
                        "errorMessage",
                        "Si è verificato un errore interno. Riprova più tardi."
                ));
    }

    @Test
    void shouldPropagateAccessDeniedException()
            throws Exception {

        jakarta.servlet.ServletException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        jakarta.servlet.ServletException.class,
                        () -> mockMvc.perform(
                                get("/test/pages/access-denied")
                                        .accept(MediaType.TEXT_HTML)
                        )
                );

        org.junit.jupiter.api.Assertions.assertInstanceOf(
                AccessDeniedException.class,
                exception.getCause()
        );
    }

    @Test
    void shouldNotReturnHtmlViewForCsvExportError()
            throws Exception {

        mockMvc.perform(
                    get("/test/pages/export.csv")
                            .accept(new MediaType("text", "csv"))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertNull(
                                result.getModelAndView(),
                                "Un errore CSV non deve restituire una vista HTML"
                        )
                )
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertFalse(
                                result.getResponse()
                                        .getContentAsString()
                                        .contains(
                                            "Dettaglio tecnico riservato"
                                        ),
                                "Il messaggio tecnico non deve essere esposto"
                        )
                );
    }

    @Test
    void shouldNotReturnHtmlViewForXlsxExportError()
            throws Exception {

        mockMvc.perform(
                    get("/test/pages/export.xlsx")
                            .accept(MediaType.parseMediaType(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            ))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertNull(
                                result.getModelAndView(),
                                "Un errore XLSX non deve restituire una vista HTML"
                        )
                )
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertFalse(
                                result.getResponse()
                                        .getContentAsString()
                                        .contains(
                                            "Dettaglio tecnico riservato"
                                        ),
                                "Il messaggio tecnico non deve essere esposto"
                        )
                );
    }

    @Test
    void shouldInspectCsvExportErrorResponse()
            throws Exception {

        var result = mockMvc.perform(
                    get("/test/pages/export.csv")
                            .accept(MediaType.APPLICATION_JSON)
                )
                .andReturn();

        var response = result.getResponse();

        System.out.println(
                "DIAGNOSTICA_CSV_STATUS=" + response.getStatus()
        );

        System.out.println(
                "DIAGNOSTICA_CSV_CONTENT_TYPE="
                        + response.getContentType()
        );

        System.out.println(
                "DIAGNOSTICA_CSV_HAS_VIEW="
                        + (result.getModelAndView() != null)
        );

        String body = response.getContentAsString();

        System.out.println(
                "DIAGNOSTICA_CSV_BODY=" + body
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                500,
                response.getStatus()
        );

        org.junit.jupiter.api.Assertions.assertNull(
                result.getModelAndView()
        );

        org.junit.jupiter.api.Assertions.assertFalse(
                body.contains("Dettaglio tecnico riservato")
        );
    }

    @Test
    void shouldReturnProblemJsonForAjaxPageError()
            throws Exception {

        var result = mockMvc.perform(
                    get("/test/pages/internal-error")
                            .header(
                                    "X-Requested-With",
                                    "XMLHttpRequest"
                            )
                            .accept(MediaType.ALL)
                )
                .andReturn();

        org.junit.jupiter.api.Assertions.assertEquals(
                500,
                result.getResponse().getStatus()
        );

        org.junit.jupiter.api.Assertions.assertNull(
                result.getModelAndView()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "application/problem+json",
                result.getResponse().getContentType()
        );

        String body = result.getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(
                body.contains(
                        "Si è verificato un errore interno."
                )
        );

        org.junit.jupiter.api.Assertions.assertFalse(
                body.contains("Dettaglio tecnico riservato")
        );
    }

    @Test
    void shouldReturnProblemJsonForAjaxBusinessException()
            throws Exception {

        var result = mockMvc.perform(
                    get("/test/pages/business")
                            .header(
                                    "X-Requested-With",
                                    "XMLHttpRequest"
                            )
                            .accept(MediaType.ALL)
                )
                .andReturn();

        org.junit.jupiter.api.Assertions.assertEquals(
                400,
                result.getResponse().getStatus()
        );

        org.junit.jupiter.api.Assertions.assertNull(
                result.getModelAndView()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "application/problem+json",
                result.getResponse().getContentType()
        );

        String body = result.getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(
                body.contains("Operazione non consentita")
        );
    }

    @Test
    void shouldReturnProblemJsonForAjaxNotFoundException()
            throws Exception {

        var result = mockMvc.perform(
                    get("/test/pages/not-found")
                            .header(
                                    "X-Requested-With",
                                    "XMLHttpRequest"
                            )
                            .accept(MediaType.ALL)
                )
                .andReturn();

        org.junit.jupiter.api.Assertions.assertEquals(
                404,
                result.getResponse().getStatus()
        );

        org.junit.jupiter.api.Assertions.assertNull(
                result.getModelAndView()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "application/problem+json",
                result.getResponse().getContentType()
        );

        String body = result.getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(
                body.contains(
                    "La risorsa richiesta non è stata trovata."
                )
        );

        org.junit.jupiter.api.Assertions.assertFalse(
                body.contains("Dettaglio tecnico interno non pubblico")
        );
    }

    @Test
    void shouldReturnProblemJsonForCsvBusinessException()
            throws Exception {

        var result = mockMvc.perform(
                    get("/test/pages/export-business.csv")
                            .accept(MediaType.APPLICATION_JSON)
                )
                .andReturn();

        org.junit.jupiter.api.Assertions.assertEquals(
                400,
                result.getResponse().getStatus()
        );

        org.junit.jupiter.api.Assertions.assertNull(
                result.getModelAndView()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "application/problem+json",
                result.getResponse().getContentType()
        );

        String body = result.getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(
                body.contains("Operazione non consentita")
        );
    }

    @Test
    void shouldReturnProblemJsonForXlsxNotFoundException()
            throws Exception {

        var result = mockMvc.perform(
                    get("/test/pages/export-not-found.xlsx")
                            .accept(MediaType.APPLICATION_JSON)
                )
                .andReturn();

        org.junit.jupiter.api.Assertions.assertEquals(
                404,
                result.getResponse().getStatus()
        );

        org.junit.jupiter.api.Assertions.assertNull(
                result.getModelAndView()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "application/problem+json",
                result.getResponse().getContentType()
        );

        String body = result.getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(
                body.contains(
                    "La risorsa richiesta non è stata trovata."
                )
        );

        org.junit.jupiter.api.Assertions.assertFalse(
                body.contains("Dettaglio tecnico interno non pubblico")
        );
    }

    @Test
    void shouldHandleCsvBusinessExceptionWithCsvAccept()
            throws Exception {

        var result = mockMvc.perform(
                    get("/test/pages/export-business.csv")
                            .accept(new MediaType("text", "csv"))
                )
                .andReturn();

        org.junit.jupiter.api.Assertions.assertEquals(
                400,
                result.getResponse().getStatus()
        );

        org.junit.jupiter.api.Assertions.assertNull(
                result.getModelAndView()
        );

        System.out.println(
                "DIAGNOSTICA_CSV_BUSINESS_STATUS="
                + result.getResponse().getStatus()
        );

        System.out.println(
                "DIAGNOSTICA_CSV_BUSINESS_CONTENT_TYPE="
                + result.getResponse().getContentType()
        );

        System.out.println(
                "DIAGNOSTICA_CSV_BUSINESS_HAS_VIEW="
                + (result.getModelAndView() != null)
        );

        System.out.println(
                "DIAGNOSTICA_CSV_BUSINESS_BODY="
                + result.getResponse().getContentAsString()
        );

        String body = result.getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertFalse(
                body.contains("Dettaglio tecnico interno")
        );
    }


    @Test
    void shouldHandleXlsxNotFoundExceptionWithXlsxAccept()
            throws Exception {

        var result = mockMvc.perform(
                    get("/test/pages/export-not-found.xlsx")
                            .accept(MediaType.parseMediaType(
                                    "application/vnd.openxmlformats-officedocument."
                                    + "spreadsheetml.sheet"
                            ))
                )
                .andReturn();

        org.junit.jupiter.api.Assertions.assertEquals(
                404,
                result.getResponse().getStatus()
        );

        org.junit.jupiter.api.Assertions.assertNull(
                result.getModelAndView()
        );

        System.out.println(
                "DIAGNOSTICA_XLSX_NOT_FOUND_STATUS="
                + result.getResponse().getStatus()
        );

        System.out.println(
                "DIAGNOSTICA_XLSX_NOT_FOUND_CONTENT_TYPE="
                + result.getResponse().getContentType()
        );

        System.out.println(
                "DIAGNOSTICA_XLSX_NOT_FOUND_HAS_VIEW="
                + (result.getModelAndView() != null)
        );

        System.out.println(
                "DIAGNOSTICA_XLSX_NOT_FOUND_BODY="
                + result.getResponse().getContentAsString()
        );

        String body = result.getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertFalse(
                body.contains("Dettaglio tecnico interno")
        );
    }


    @Test
    void shouldPropagateAccessDeniedExceptionFromRestController()
            throws Exception {

        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(
                        get("/test/errors/rest-access-denied")
                                .accept(MediaType.APPLICATION_JSON)
                ).andReturn()
        );

        org.junit.jupiter.api.Assertions.assertInstanceOf(
                org.springframework.security.access.AccessDeniedException.class,
                exception.getCause()
        );
    }

    @Test
    void shouldNotExposeTechnicalDetailsForRestNotFoundException()
            throws Exception {

        mockMvc.perform(
                    get("/test/errors/rest-not-found")
                            .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.detail").value(
                        "La risorsa richiesta non è stata trovata."
                ))
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertFalse(
                                result.getResponse()
                                        .getContentAsString()
                                        .contains(
                                            "Dettaglio tecnico interno riservato"
                                        )
                        )
                );
    }

    @RestController
    static class TestRestController {

        @GetMapping("/test/errors/rest-not-found")
        public String restNotFound() {
            throw new NotFoundException(
                    "Dettaglio tecnico interno riservato"
            );
        }


        @GetMapping("/test/errors/rest-access-denied")
        public String restAccessDenied() {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Dettaglio autorizzazione interno"
            );
        }

        @GetMapping("/test/errors/business")
        public String businessError() {
            throw new BusinessException(
                    "Operazione non consentita"
            );
        }
    }
}
