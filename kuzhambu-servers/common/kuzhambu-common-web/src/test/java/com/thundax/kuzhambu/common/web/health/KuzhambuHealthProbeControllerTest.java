package com.thundax.kuzhambu.common.web.health;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class KuzhambuHealthProbeControllerTest {

    @Test
    public void shouldReturnUpForLivenessAndReadiness() {
        KuzhambuHealthProbeController controller = new KuzhambuHealthProbeController();

        assertEquals("UP", controller.liveness().get("status"));
        assertEquals("UP", controller.readiness().get("status"));
    }
}
