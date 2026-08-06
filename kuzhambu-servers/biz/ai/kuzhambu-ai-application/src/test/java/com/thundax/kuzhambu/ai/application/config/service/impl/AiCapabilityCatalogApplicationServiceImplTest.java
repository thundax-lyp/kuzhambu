package com.thundax.kuzhambu.ai.application.config.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.ai.application.config.query.GetAiCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiCapabilitiesQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptCapabilityVariablesQuery;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import org.junit.jupiter.api.Test;

class AiCapabilityCatalogApplicationServiceImplTest {

    @Test
    void getCapabilityShouldPreserveBusinessCapabilityType() {
        AiCapabilityCatalogApplicationServiceImpl service = new AiCapabilityCatalogApplicationServiceImpl();

        assertThat(service.get(new GetAiCapabilityQuery(AiBusinessCapability.CLASSICS_TRANSLATE)))
                .isEqualTo(AiBusinessCapability.CLASSICS_TRANSLATE);
    }

    @Test
    void listCapabilitiesShouldReturnBusinessCapabilitiesWhenEnabled() {
        AiCapabilityCatalogApplicationServiceImpl service = new AiCapabilityCatalogApplicationServiceImpl();

        assertThat(service.list(new ListAiCapabilitiesQuery(true))).contains(AiBusinessCapability.CLASSICS_TRANSLATE);
    }

    @Test
    void listCapabilitiesShouldReturnEmptyWhenDisabledFilterIsFalse() {
        AiCapabilityCatalogApplicationServiceImpl service = new AiCapabilityCatalogApplicationServiceImpl();

        assertThat(service.list(new ListAiCapabilitiesQuery(false))).isEmpty();
    }

    @Test
    void listPromptVariablesShouldCoverEveryBusinessCapability() {
        AiCapabilityCatalogApplicationServiceImpl service = new AiCapabilityCatalogApplicationServiceImpl();

        for (AiBusinessCapability capability : AiBusinessCapability.values()) {
            assertThat(service.listPromptVariables(new ListPromptCapabilityVariablesQuery(capability)))
                    .as("prompt variables for %s", capability)
                    .isNotEmpty();
        }
    }

    @Test
    void listPromptVariablesShouldExposeKnowledgeGraphContract() {
        AiCapabilityCatalogApplicationServiceImpl service = new AiCapabilityCatalogApplicationServiceImpl();

        assertThat(service.listPromptVariables(
                        new ListPromptCapabilityVariablesQuery(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT)))
                .extracting("variableName")
                .containsExactly("sourceTitle", "sourceText", "entryRefs");
    }
}
