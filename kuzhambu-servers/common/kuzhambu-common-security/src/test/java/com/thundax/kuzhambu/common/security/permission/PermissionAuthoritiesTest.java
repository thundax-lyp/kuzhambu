package com.thundax.kuzhambu.common.security.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class PermissionAuthoritiesTest {

    @Test
    public void shouldConvertSpringAuthoritiesToPermissions() {
        Set<String> permissions = PermissionAuthorities.toPermissions(
                Arrays.asList(new SimpleGrantedAuthority("user"), new SimpleGrantedAuthority("sys:role")));

        assertTrue(permissions.contains("user"));
        assertTrue(permissions.contains("sys:role"));
        assertEquals(2, permissions.size());
    }
}
