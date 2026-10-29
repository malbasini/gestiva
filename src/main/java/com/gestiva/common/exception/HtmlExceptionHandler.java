package com.gestiva.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(annotations = Controller.class)
public class HtmlExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleBusiness(
            BusinessException ex,
            Model model,
            HttpServletRequest request
    ) {
        boolean ajaxRequest = "XMLHttpRequest".equalsIgnoreCase(
                request.getHeader("X-Requested-With")
        );

        Object handler = request.getAttribute(
                org.springframework.web.servlet.HandlerMapping
                        .BEST_MATCHING_HANDLER_ATTRIBUTE
        );

        boolean responseEntityHandler = false;

        if (handler instanceof HandlerMethod handlerMethod) {
            MethodParameter returnType =
                    handlerMethod.getReturnType();

            responseEntityHandler = ResponseEntity.class.isAssignableFrom(
                    returnType.getParameterType()
            );
        }

        if (ajaxRequest || responseEntityHandler) {
            ProblemDetail problem = ProblemDetail.forStatus(
                    HttpStatus.BAD_REQUEST
            );

            problem.setTitle("Business error");
            problem.setDetail(ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(problem);
        }

        model.addAttribute(
                "errorMessage",
                ex.getMessage()
        );

        return "error/business-error";
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object handleNotFound(
            NotFoundException ex,
            Model model,
            HttpServletRequest request
    ) {
        String safeMessage =
                "La risorsa richiesta non è stata trovata.";

        boolean ajaxRequest = "XMLHttpRequest".equalsIgnoreCase(
                request.getHeader("X-Requested-With")
        );

        Object handler = request.getAttribute(
                org.springframework.web.servlet.HandlerMapping
                        .BEST_MATCHING_HANDLER_ATTRIBUTE
        );

        boolean responseEntityHandler = false;

        if (handler instanceof HandlerMethod handlerMethod) {
            MethodParameter returnType =
                    handlerMethod.getReturnType();

            responseEntityHandler = ResponseEntity.class.isAssignableFrom(
                    returnType.getParameterType()
            );
        }

        if (ajaxRequest || responseEntityHandler) {
            ProblemDetail problem = ProblemDetail.forStatus(
                    HttpStatus.NOT_FOUND
            );

            problem.setTitle("Resource not found");
            problem.setDetail(safeMessage);

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(problem);
        }

        model.addAttribute("errorMessage", safeMessage);

        return "error/business-error";
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpected(
            Exception ex,
            Model model,
            HttpServletRequest request
    ) throws Exception {

        if (ex instanceof AccessDeniedException
                || ex instanceof AuthenticationException) {
            throw ex;
        }

        String safeMessage =
                "Si è verificato un errore interno. Riprova più tardi.";

        Object handler = request.getAttribute(
                org.springframework.web.servlet.HandlerMapping
                        .BEST_MATCHING_HANDLER_ATTRIBUTE
        );

        boolean responseEntityHandler = false;

        if (handler instanceof HandlerMethod handlerMethod) {
            MethodParameter returnType =
                    handlerMethod.getReturnType();

            responseEntityHandler = ResponseEntity.class.isAssignableFrom(
                    returnType.getParameterType()
            );
        }

        boolean ajaxRequest = "XMLHttpRequest".equalsIgnoreCase(
                request.getHeader("X-Requested-With")
        );

        if (responseEntityHandler || ajaxRequest) {
            ProblemDetail problem = ProblemDetail.forStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

            problem.setTitle("Internal server error");
            problem.setDetail(safeMessage);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(problem);
        }

        model.addAttribute("errorMessage", safeMessage);

        return new org.springframework.web.servlet.ModelAndView(
                "error/business-error",
                model.asMap(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
