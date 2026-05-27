package com.thundax.kuzhambu.classics.interfaces.admin.sancai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.SancaiAdminController;
import org.junit.jupiter.api.Test;

class SancaiAdminControllerTest {
    @Test
    void controllerTypeShouldExist() {
        assertNotNull(SancaiAdminController.class);
    }
}
