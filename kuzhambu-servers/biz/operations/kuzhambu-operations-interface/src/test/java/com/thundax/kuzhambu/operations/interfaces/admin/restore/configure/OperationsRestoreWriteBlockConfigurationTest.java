package com.thundax.kuzhambu.operations.interfaces.admin.restore.configure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.web.restore.RestoreWriteBlockState;
import com.thundax.kuzhambu.operations.application.restore.support.OperationsRestoreWriteBlocker;
import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import org.junit.jupiter.api.Test;

class OperationsRestoreWriteBlockConfigurationTest {

    @Test
    void webWriteBlockerShouldToggleCommonWebState() {
        RestoreWriteBlockState state = new RestoreWriteBlockState();
        OperationsRestoreWriteBlocker blocker =
                new OperationsRestoreWriteBlockConfiguration.WebOperationsRestoreWriteBlocker(state);

        blocker.enable(RestoreId.of(9101L));

        assertTrue(blocker.isEnabled());
        assertTrue(state.isBlocked());

        blocker.disable(RestoreId.of(9101L));

        assertFalse(blocker.isEnabled());
        assertFalse(state.isBlocked());
    }
}
