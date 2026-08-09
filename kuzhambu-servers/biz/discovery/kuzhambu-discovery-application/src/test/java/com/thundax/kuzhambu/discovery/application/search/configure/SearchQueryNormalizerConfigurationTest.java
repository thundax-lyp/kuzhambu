package com.thundax.kuzhambu.discovery.application.search.configure;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.discovery.domain.search.support.SearchQueryNormalizer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SearchQueryNormalizerConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SearchQueryNormalizerConfiguration.class));

    @Test
    void shouldCreateSearchQueryNormalizerBean() {
        contextRunner.run(context -> assertNotNull(context.getBean(SearchQueryNormalizer.class)));
    }
}
