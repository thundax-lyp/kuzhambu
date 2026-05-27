package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.MingCustomsAdminController;
import org.junit.jupiter.api.Test;

class MingCustomsAdminControllerTest {
    @Test
    void controllerTypeShouldExist() {
        assertNotNull(MingCustomsAdminController.class);
    }
}
