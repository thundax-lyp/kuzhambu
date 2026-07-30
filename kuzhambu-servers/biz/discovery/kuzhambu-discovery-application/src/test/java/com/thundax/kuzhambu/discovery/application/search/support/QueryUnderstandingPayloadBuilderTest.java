package com.thundax.kuzhambu.discovery.application.search.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QueryUnderstandingPayloadBuilderTest {

    @Test
    void buildOutputSchemaJsonShouldUsePromptIntentTypeField() {
        String schema = new QueryUnderstandingPayloadBuilder().buildOutputSchemaJson();

        assertTrue(schema.contains("\"intentType\""));
        assertFalse(schema.contains("\"intent\""));
    }
}
