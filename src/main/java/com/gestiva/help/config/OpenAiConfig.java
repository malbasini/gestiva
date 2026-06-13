package com.gestiva.help.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    @Bean
    public OpenAIClient openAIClient(OpenAiProperties properties) {
        return OpenAIOkHttpClient.builder()
                .apiKey(properties.getApiKey())
                .build();
    }
}