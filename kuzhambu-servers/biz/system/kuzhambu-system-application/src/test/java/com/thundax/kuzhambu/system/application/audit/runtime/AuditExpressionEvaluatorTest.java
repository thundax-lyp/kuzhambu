package com.thundax.kuzhambu.system.application.audit.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.common.audit.runtime.AuditSnapshots;
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
}
