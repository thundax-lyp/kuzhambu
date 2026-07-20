package com.thundax.kuzhambu.common.audit.runtime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

public class AuditObjectLoaderRegistryTest {

    @Test
    public void shouldGetRegisteredLoader() {
        AuditObjectLoader loader = new TestAuditObjectLoader("book");

        AuditObjectLoaderRegistry registry = new AuditObjectLoaderRegistry(List.of(loader));

        assertSame(loader, registry.get("book"));
    }

    @Test
    public void shouldRejectDuplicateObjectType() {
        assertThrows(
                IllegalStateException.class,
                () -> new AuditObjectLoaderRegistry(
                        List.of(new TestAuditObjectLoader("book"), new TestAuditObjectLoader("book"))));
    }

    private record TestAuditObjectLoader(String objectType) implements AuditObjectLoader {

        @Override
        public Object load(String objectId) {
            return objectId;
        }
    }
}
