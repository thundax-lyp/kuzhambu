package com.thundax.kuzhambu.ai.application.config.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import org.junit.jupiter.api.Test;

class AiCapabilityCatalogApplicationServiceImplTest {

    @Test
    void getCapabilityShouldPreserveBusinessCapabilityType() {
        AiCapabilityCatalogApplicationServiceImpl service = new AiCapabilityCatalogApplicationServiceImpl();

        assertThat(service.getCapability(AiBusinessCapability.CLASSICS_TRANSLATE))
                .isEqualTo(AiBusinessCapability.CLASSICS_TRANSLATE);
    }

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
