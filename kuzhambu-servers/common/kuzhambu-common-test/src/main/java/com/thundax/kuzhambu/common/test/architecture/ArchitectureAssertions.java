package com.thundax.kuzhambu.common.test.architecture;

import org.junit.jupiter.api.Assertions;

final class ArchitectureAssertions {

    private ArchitectureAssertions() {}

    static void assertTrue(String message, boolean condition) {
        Assertions.assertTrue(condition, message);
    }
}
