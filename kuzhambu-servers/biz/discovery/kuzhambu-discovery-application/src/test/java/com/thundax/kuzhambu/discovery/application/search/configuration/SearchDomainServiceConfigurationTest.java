package com.thundax.kuzhambu.discovery.application.search.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.discovery.domain.service.SearchDomainService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SearchDomainServiceConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SearchDomainServiceConfiguration.class));

    @Test
    void shouldCreateSearchDomainServiceBean() {
        contextRunner.run(context -> assertNotNull(context.getBean(SearchDomainService.class)));
    }
}
