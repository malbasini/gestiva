package com.gestiva.sales.quote.controller;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.sales.quote.dto.QuoteCreateRequest;
import com.gestiva.sales.quote.dto.QuoteResponse;
import com.gestiva.sales.quote.dto.QuoteSearchRequest;
import com.gestiva.sales.quote.dto.QuoteUpdateRequest;
import com.gestiva.sales.quote.service.QuoteService;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public PageResponse<QuoteResponse> search(@RequestParam(required = false) String status,
                                              @RequestParam(required = false) Long customerId,
                                              @RequestParam(required = false) String search,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(defaultValue = "quoteDate") String sortBy,
                                              @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        QuoteSearchRequest request = new QuoteSearchRequest();
        request.setStatus(status);
        request.setCustomerId(customerId);
        request.setSearch(search);

        return quoteService.search(tenantContext.getCurrentTenantId(), request, pageable);
    }

    @GetMapping("/{id}")
    public QuoteResponse findById(@PathVariable Long id) {
        return quoteService.findById(tenantContext.getCurrentTenantId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse create(@Valid @RequestBody QuoteCreateRequest request) {
        return quoteService.create(tenantContext.getCurrentTenantId(), request);
    }

    @PutMapping("/{id}")
    public QuoteResponse update(@PathVariable Long id,
                                @Valid @RequestBody QuoteUpdateRequest request) {
        return quoteService.update(tenantContext.getCurrentTenantId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        quoteService.delete(tenantContext.getCurrentTenantId(), id);
    }
}