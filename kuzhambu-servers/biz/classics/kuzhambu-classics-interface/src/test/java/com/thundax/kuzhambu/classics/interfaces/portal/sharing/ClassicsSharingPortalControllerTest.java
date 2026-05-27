package com.thundax.kuzhambu.classics.interfaces.portal.sharing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.ClassicsSharingPortalController;
import org.junit.jupiter.api.Test;

class ClassicsSharingPortalControllerTest {
    @Test
    void controllerTypeShouldExist() {
        assertNotNull(ClassicsSharingPortalController.class);
    }
}
