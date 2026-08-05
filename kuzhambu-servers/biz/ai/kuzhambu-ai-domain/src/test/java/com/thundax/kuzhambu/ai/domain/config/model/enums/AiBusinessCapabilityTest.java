package com.thundax.kuzhambu.ai.domain.config.model.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AiBusinessCapabilityTest {

    @Test
    void fromShouldResolveClassicsTagCode() {
        assertEquals(AiBusinessCapability.CLASSICS_TAG_EXTRACT, AiBusinessCapability.from("CLASSICS_TAG_EXTRACT"));
    }

    @Test
    void fromShouldResolveLegacyStoredCapabilityCodes() {
        assertEquals(AiBusinessCapability.CLASSICS_TAG_EXTRACT, AiBusinessCapability.from("classics_tags"));
        assertEquals(AiBusinessCapability.KNOWLEDGE_TAG_EXTRACT, AiBusinessCapability.from("knowledge_tags"));
        assertEquals(AiBusinessCapability.PROMPT_SUGGEST, AiBusinessCapability.from("prompt_suggestion"));
    }
}
