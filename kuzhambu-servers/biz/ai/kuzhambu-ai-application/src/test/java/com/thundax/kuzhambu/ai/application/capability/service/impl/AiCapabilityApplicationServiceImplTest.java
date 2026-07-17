package com.thundax.kuzhambu.ai.application.capability.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import org.junit.jupiter.api.Test;

class AiCapabilityApplicationServiceImplTest {

    @Test
    void listCapabilitiesShouldReturnBusinessCapabilitiesWhenEnabled() {
        AiCapabilityApplicationServiceImpl service = new AiCapabilityApplicationServiceImpl();

        assertThat(service.listCapabilities(true)).contains(AiBusinessCapability.CLASSICS_TRANSLATE);
    }

    @Test
    void listCapabilitiesShouldReturnEmptyWhenDisabledFilterIsFalse() {
        AiCapabilityApplicationServiceImpl service = new AiCapabilityApplicationServiceImpl();

        assertThat(service.listCapabilities(false)).isEmpty();
    }
}
