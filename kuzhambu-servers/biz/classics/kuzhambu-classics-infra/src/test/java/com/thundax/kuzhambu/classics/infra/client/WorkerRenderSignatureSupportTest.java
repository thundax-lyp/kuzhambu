package com.thundax.kuzhambu.classics.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WorkerRenderSignatureSupportTest {

    private final WorkerRenderSignatureSupport signatureSupport = new WorkerRenderSignatureSupport();

    @Test
    void signingInputShouldFollowWorkerRenderContract() {
        String body = "{\"x\":1}";

        assertEquals("5041bf1f713df204784353e82f6a4a535931cb64f1f4b4a5aeaffcb720918b22", signatureSupport.sha256(body));
        assertEquals(
                "POST\n"
                        + "/internal/render/classics-export\n"
                        + "1710000000000\n"
                        + "req-1\n"
                        + "5041bf1f713df204784353e82f6a4a535931cb64f1f4b4a5aeaffcb720918b22",
                signatureSupport.signingInput(
                        "POST", "/internal/render/classics-export", "1710000000000", "req-1", body));
        assertEquals(
                "e3f6590c755f86d638a11f4ecebdc33f86a71f9a9808afd1c030e63fecc7065c",
                signatureSupport.sign(
                        "POST", "/internal/render/classics-export", "1710000000000", "req-1", body, "secret"));
    }

    @Test
    void signShouldRejectBlankSecret() {
        assertThrows(
                IllegalStateException.class,
                () -> signatureSupport.sign("POST", "/internal/render/classics-export", "1", "req-1", "{}", " "));
    }
}
