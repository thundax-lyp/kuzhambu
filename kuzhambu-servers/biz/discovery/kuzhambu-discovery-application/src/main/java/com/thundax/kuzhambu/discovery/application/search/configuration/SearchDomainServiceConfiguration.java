package com.thundax.kuzhambu.discovery.application.search.configuration;

import com.thundax.kuzhambu.discovery.domain.service.SearchDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchDomainServiceConfiguration {

    @Bean
    public SearchDomainService searchDomainService() {
        return new SearchDomainService();
    }
}
