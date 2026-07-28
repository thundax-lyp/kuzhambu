package com.thundax.kuzhambu.ai.application.config.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import org.junit.jupiter.api.Test;

class AiCapabilityCatalogApplicationServiceImplTest {

    @Test
    void listCapabilitiesShouldReturnBusinessCapabilitiesWhenEnabled() {
        AiCapabilityCatalogApplicationServiceImpl service = new AiCapabilityCatalogApplicationServiceImpl();

        assertThat(service.listCapabilities(true)).contains(AiBusinessCapability.CLASSICS_TRANSLATE);
    }

    @Test
    void listCapabilitiesShouldReturnEmptyWhenDisabledFilterIsFalse() {
        AiCapabilityCatalogApplicationServiceImpl service = new AiCapabilityCatalogApplicationServiceImpl();

        assertThat(service.listCapabilities(false)).isEmpty();
    }
}
