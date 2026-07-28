package com.thundax.kuzhambu.ai.infra.invocation.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AiWorkerRequestSignerTest {

    private final AiWorkerRequestSigner requestSigner = new AiWorkerRequestSigner();

    @Test
    void signingInputShouldFollowWorkerContract() {
        String body = "{\"x\":1}";

        assertEquals("5041bf1f713df204784353e82f6a4a535931cb64f1f4b4a5aeaffcb720918b22", requestSigner.sha256(body));
        assertEquals(
                "POST\n"
                        + "/internal/ai/invoke\n"
                        + "1710000000000\n"
                        + "req-1\n"
                        + "5041bf1f713df204784353e82f6a4a535931cb64f1f4b4a5aeaffcb720918b22",
                requestSigner.signingInput("POST", "/internal/ai/invoke", "1710000000000", "req-1", body));
        assertEquals(
                "1dbdaa72ffa3b23a8926863320c2eea0a2fadb1f4f766378eb3943f1fa03a4a5",
                requestSigner.sign("POST", "/internal/ai/invoke", "1710000000000", "req-1", body, "secret"));
    }

    @Test
    void signShouldRejectBlankSecret() {
        assertThrows(
                IllegalStateException.class,
                () -> requestSigner.sign("POST", "/internal/ai/invoke", "1", "req-1", "{}", " "));
    }
}
