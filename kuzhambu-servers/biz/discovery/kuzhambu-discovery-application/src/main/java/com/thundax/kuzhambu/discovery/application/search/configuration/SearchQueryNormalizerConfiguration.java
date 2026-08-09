package com.thundax.kuzhambu.discovery.application.search.configuration;

import com.thundax.kuzhambu.discovery.domain.search.support.SearchQueryNormalizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchQueryNormalizerConfiguration {

    @Bean
    public SearchQueryNormalizer searchQueryNormalizer() {
        return new SearchQueryNormalizer();
    }
}
