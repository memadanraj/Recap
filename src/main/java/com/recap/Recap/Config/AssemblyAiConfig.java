package com.recap.Recap.Config;

import com.assemblyai.api.AssemblyAI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class AssemblyAiConfig {

    @Value("${assemblyai.api-key}")
    private String apiKey;

    @Bean
    public AssemblyAI assemblyAIClient() {
        return AssemblyAI.builder()
                .apiKey(apiKey)
                .build();
    }

}
