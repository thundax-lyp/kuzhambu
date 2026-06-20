package com.thundax.kuzhambu.classics.domain.sharing.model.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClassicsShareTargetStatusTest {

    @Test
    void activeShouldBeReadAsAvailableForExistingShareTargets() {
        assertEquals(ClassicsShareTargetStatus.AVAILABLE, ClassicsShareTargetStatus.from("ACTIVE"));
    }
}
