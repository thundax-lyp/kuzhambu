package com.thundax.kuzhambu.classics.application.content.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ClassicsContentPermissionSupportTest {

    @Test
    void shouldResolveContentTypePermissions() {
        assertEquals(
                "classics:sancai:view",
                ClassicsContentPermissionSupport.viewPermission(ClassicsContentType.SANCAI_ENTRY));
        assertEquals(
                "classics:sancai:edit",
                ClassicsContentPermissionSupport.editPermission(ClassicsContentType.SANCAI_ENTRY));
        assertEquals(
                "classics:wangqi:view",
                ClassicsContentPermissionSupport.viewPermission(ClassicsContentType.WANGQI_DOCUMENT));
        assertEquals(
                "classics:mingcustoms:edit",
                ClassicsContentPermissionSupport.editPermission(ClassicsContentType.MING_CUSTOMS));
    }

    @Test
    void shouldCheckViewEditAndExportPermissions() {
        Set<String> permissions = Set.of("classics:sancai:view", "classics:sancai:edit", "classics:content:export");

        assertTrue(ClassicsContentPermissionSupport.canView(ClassicsContentType.SANCAI_ENTRY, permissions));
        assertTrue(ClassicsContentPermissionSupport.canEdit(ClassicsContentType.SANCAI_ENTRY, permissions));
        assertTrue(ClassicsContentPermissionSupport.canExport(ClassicsContentType.SANCAI_ENTRY, permissions));
        assertFalse(ClassicsContentPermissionSupport.canView(ClassicsContentType.WANGQI_DOCUMENT, permissions));
    }

    @Test
    void shouldDenyWhenPermissionSetIsNull() {
        assertFalse(ClassicsContentPermissionSupport.canView(ClassicsContentType.SANCAI_ENTRY, null));
        assertFalse(ClassicsContentPermissionSupport.canEdit(ClassicsContentType.SANCAI_ENTRY, null));
        assertFalse(ClassicsContentPermissionSupport.canExport(ClassicsContentType.SANCAI_ENTRY, null));
    }
}
