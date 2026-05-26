package com.thundax.kuzhambu.common.web.health;

import java.util.Collections;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KuzhambuHealthProbeController {

    private static final Map<String, String> UP = Collections.singletonMap("status", "UP");

    @GetMapping("/actuator/health/liveness")
    public Map<String, String> liveness() {
        return UP;
    }

    @GetMapping("/actuator/health/readiness")
    public Map<String, String> readiness() {
        return UP;
    }
}
