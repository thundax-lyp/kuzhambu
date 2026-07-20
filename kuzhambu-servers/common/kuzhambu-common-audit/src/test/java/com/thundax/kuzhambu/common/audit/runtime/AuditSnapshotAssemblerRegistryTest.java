package com.thundax.kuzhambu.common.audit.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.audit.model.valueobject.AuditField;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AuditSnapshotAssemblerRegistryTest {

    @Test
    public void shouldListAssemblersByObjectType() {
        AuditSnapshotAssembler userAssembler = new TestAuditSnapshotAssembler("user");
        AuditSnapshotAssembler bookAssembler = new TestAuditSnapshotAssembler("book");

        AuditSnapshotAssemblerRegistry registry =
                new AuditSnapshotAssemblerRegistry(List.of(userAssembler, bookAssembler));

        assertSame(bookAssembler, registry.get("book"));
        assertEquals(List.of(bookAssembler, userAssembler), registry.list());
    }

    @Test
    public void shouldRejectDuplicateObjectType() {
        assertThrows(
                IllegalStateException.class,
                () -> new AuditSnapshotAssemblerRegistry(
                        List.of(new TestAuditSnapshotAssembler("book"), new TestAuditSnapshotAssembler("book"))));
    }

    private record TestAuditSnapshotAssembler(String objectType) implements AuditSnapshotAssembler {

        @Override
        public String objectTypeLabel() {
            return objectType;
        }

        @Override
        public List<AuditField> fields() {
            return Collections.emptyList();
        }

        @Override
        public AuditSnapshot assemble(Object object) {
            return new AuditSnapshot();
        }
    }
}
