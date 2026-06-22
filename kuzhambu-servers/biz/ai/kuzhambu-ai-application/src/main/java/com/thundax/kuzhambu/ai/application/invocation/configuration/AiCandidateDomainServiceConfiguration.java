package com.thundax.kuzhambu.ai.application.invocation.configuration;

import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCandidateDomainServiceConfiguration {

    @Bean
    public AiCandidateDomainService aiCandidateDomainService(AiInvocationRepository repository) {
        return new AiCandidateDomainService(repository);
    }
}
