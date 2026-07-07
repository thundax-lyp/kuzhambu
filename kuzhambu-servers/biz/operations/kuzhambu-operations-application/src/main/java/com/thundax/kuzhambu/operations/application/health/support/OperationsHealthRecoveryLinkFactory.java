package com.thundax.kuzhambu.operations.application.health.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class OperationsHealthRecoveryLinkFactory {

    public static final String ACTION_OPEN_HEALTH_DETAIL = "OPEN_HEALTH_DETAIL";

    public String healthDetailTarget(String component) {
        return "{\"route\":\"/operations/dashboard\",\"component\":\"" + escapeJson(component) + "\"}";
    }

    private static String escapeJson(String value) {
        return StringUtils.defaultString(value).replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
