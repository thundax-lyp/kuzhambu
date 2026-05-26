package com.thundax.kuzhambu.common.security.permission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class PermissionAuthorizationServiceTest {

    @AfterEach
    public void tearDown() {
        KuzhambuContextHolder.clear();
    }

    @Test
    public void shouldPermitWhenAnyRequiredPermissionMatches() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "user-1", KuzhambuSubjectType.ADMIN_USER, "Admin", "token-1", Arrays.asList("sys:user:view")));
        PermissionAuthorizationService service = new PermissionAuthorizationService(new PrefixPermissionMatcher());

        assertTrue(service.isPermittedAny("sys:role:view", "sys:user:view"));
    }

    @Test
    public void shouldDenyAnonymousUser() {
        PermissionAuthorizationService service = new PermissionAuthorizationService(new PrefixPermissionMatcher());

        assertFalse(service.isPermitted("sys:user:view"));
    }
}
