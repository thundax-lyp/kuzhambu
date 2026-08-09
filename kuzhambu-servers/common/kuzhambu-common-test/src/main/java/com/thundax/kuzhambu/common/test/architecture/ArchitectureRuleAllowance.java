package com.thundax.kuzhambu.common.test.architecture;

public record ArchitectureRuleAllowance(String key, String description, String remediation) {

    public ArchitectureRuleAllowance {
        if (isBlank(key)) {
            throw new IllegalArgumentException("Architecture allowlist key must not be blank");
        }
        if (isBlank(description)) {
            throw new IllegalArgumentException("Architecture allowlist description must not be blank");
        }
        if (isBlank(remediation)) {
            throw new IllegalArgumentException("Architecture allowlist remediation must not be blank");
        }
    }

    public static ArchitectureRuleAllowance of(String key, String description, String remediation) {
        return new ArchitectureRuleAllowance(key, description, remediation);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
