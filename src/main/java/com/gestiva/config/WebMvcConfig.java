package com.gestiva.config;

import com.gestiva.billing.subscription.web.TenantSubscriptionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantSubscriptionInterceptor tenantSubscriptionInterceptor;

    public WebMvcConfig(TenantSubscriptionInterceptor tenantSubscriptionInterceptor) {
        this.tenantSubscriptionInterceptor = tenantSubscriptionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantSubscriptionInterceptor)
                .addPathPatterns("/**");
    }
}