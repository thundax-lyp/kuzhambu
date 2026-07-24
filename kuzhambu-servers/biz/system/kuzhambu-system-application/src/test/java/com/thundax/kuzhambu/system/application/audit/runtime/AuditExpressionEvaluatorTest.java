package com.thundax.kuzhambu.system.application.audit.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.common.audit.runtime.AuditSnapshots;
import com.thundax.kuzhambu.system.application.core.command.ChangeUserStatusCommand;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class AuditExpressionEvaluatorTest {

    @Test
    void diffShouldRecordRemovedFields() {
        var before = AuditSnapshots.of("User", 1L, "管理员", AuditSnapshots.field("name", "名称", "管理员"));

        var changedFields = AuditExpressionEvaluator.diff(before, null);

        assertEquals(1, changedFields.size());
        assertEquals("name", changedFields.get(0).getFieldName());
        assertEquals("管理员", changedFields.get(0).getBeforeValue());
        assertNull(changedFields.get(0).getAfterValue());
    }

    @Test
    void objectValueShouldBindCommandAliasForSingleArgument() throws NoSuchMethodException {
        Method method =
                AuditExpressionEvaluatorTest.class.getDeclaredMethod("changeStatus", ChangeUserStatusCommand.class);
        ChangeUserStatusCommand command = new ChangeUserStatusCommand(UserId.of(4L), UserStatus.DISABLED);

        Object value = AuditExpressionEvaluator.objectValue("#command.id.value()", method, new Object[] {command});

        assertEquals(4L, value);
    }

    @SuppressWarnings("unused")
    private void changeStatus(ChangeUserStatusCommand command) {}
}
