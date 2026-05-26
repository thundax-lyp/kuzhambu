package com.thundax.kuzhambu.common.security.annotation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

public class HasPermissionTest {

    @Test
    public void shouldReadTypePermissionAtRuntime() {
        HasPermission annotation = PermissionController.class.getAnnotation(HasPermission.class);

        assertNotNull(annotation);
        assertArrayEquals(new String[] {"sys:user:view"}, annotation.value());
    }

    @Test
    public void shouldReadMethodPermissionAtRuntime() throws Exception {
        Method method = PermissionController.class.getDeclaredMethod("save");
        HasPermission annotation = method.getAnnotation(HasPermission.class);

        assertNotNull(annotation);
        assertArrayEquals(new String[] {"sys:user:add", "sys:user:edit"}, annotation.value());
    }

    @HasPermission("sys:user:view")
    private static class PermissionController {

        @HasPermission({"sys:user:add", "sys:user:edit"})
        public void save() {}
    }
}
