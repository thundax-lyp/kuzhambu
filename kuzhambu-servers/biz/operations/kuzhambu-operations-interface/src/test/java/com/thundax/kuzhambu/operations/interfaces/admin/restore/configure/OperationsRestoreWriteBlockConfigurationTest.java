package com.thundax.kuzhambu.operations.interfaces.admin.restore.configure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.web.restore.RestoreWriteBlockState;
import com.thundax.kuzhambu.operations.application.restore.support.OperationsRestoreWriteBlocker;
import com.thundax.kuzhambu.operations.domain.restore.codec.RestoreIdCodec;
import org.junit.jupiter.api.Test;

class OperationsRestoreWriteBlockConfigurationTest {

    @Test
    void webWriteBlockerShouldToggleCommonWebState() {
        RestoreWriteBlockState state = new RestoreWriteBlockState();
        OperationsRestoreWriteBlocker blocker =
                new OperationsRestoreWriteBlockConfiguration.WebOperationsRestoreWriteBlocker(state);

        blocker.enable(RestoreIdCodec.toDomain(9101L));

        assertTrue(blocker.isEnabled());
        assertTrue(state.isBlocked());

        blocker.disable(RestoreIdCodec.toDomain(9101L));

        assertFalse(blocker.isEnabled());
        assertFalse(state.isBlocked());
    }
}
