package com.gestiva.sales.quote.controller;

import com.gestiva.sales.quote.dto.QuoteCreateRequest;
import com.gestiva.sales.quote.dto.QuoteResponse;
import com.gestiva.sales.quote.dto.QuoteUpdateRequest;
import com.gestiva.sales.quote.service.QuoteService;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;
    private final TenantContext tenantContext;

    public QuoteController(QuoteService quoteService,
                           TenantContext tenantContext) {
        this.quoteService = quoteService;
        this.tenantContext = tenantContext;
    }

    private Long getTenantId() {
        return tenantContext.getCurrentTenantId();
    }

    @GetMapping

    public List<QuoteResponse> findAll() {

        return quoteService.findAll(getTenantId());

    }

    @GetMapping("/{id}")
    public QuoteResponse findById(@PathVariable Long id) {
        return quoteService.findById(getTenantId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse create(@Valid @RequestBody QuoteCreateRequest request) {
        quoteService.validateDates(request.getQuoteDate(), request.getValidUntil());
        return quoteService.create(getTenantId(), request);
    }
    @PutMapping("/{id}")
    public QuoteResponse update(@PathVariable Long id,
                                @Valid @RequestBody QuoteUpdateRequest request) {

        quoteService.validateDates(request.getQuoteDate(), request.getValidUntil());
        return quoteService.update(getTenantId(), id, request);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        quoteService.delete(getTenantId(), id);

    }

}