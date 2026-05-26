package com.thundax.kuzhambu.common.security.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class KuzhambuContextHolderTest {

    @AfterEach
    public void tearDown() {
        KuzhambuContextHolder.clear();
    }

    @Test
    public void shouldKeepRequestContextSeparateFromSubject() {
        KuzhambuContextHolder.setRequestId("request-1");
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "user-1", KuzhambuSubjectType.ADMIN_USER, "admin", "token-1", Collections.singleton("sys:user:list")));

        KuzhambuContextHolder.clearRequestContext();

        assertNull(KuzhambuContextHolder.requestId());
        assertEquals("user-1", KuzhambuContextHolder.currentSubjectId());
        assertEquals("token-1", KuzhambuContextHolder.currentToken());
        assertTrue(KuzhambuContextHolder.currentAuthorities().contains("sys:user:list"));
    }

    @Test
    public void shouldClearRequestContextAndSubjectTogether() {
        KuzhambuContextHolder.setRequestId("request-1");
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "user-1", KuzhambuSubjectType.ADMIN_USER, "admin", "token-1", Collections.emptySet()));

        KuzhambuContextHolder.clear();

        assertNull(KuzhambuContextHolder.requestId());
        assertNull(KuzhambuContextHolder.currentSubjectId());
    }

    @Test
    public void shouldReturnAuthoritySnapshot() {
        KuzhambuSubject subject = new KuzhambuSubject(
                "user-1", KuzhambuSubjectType.ADMIN_USER, "admin", "token-1", Arrays.asList("sys:user:list"));

        Set<String> authorities = subject.getAuthorities();
        subject.setAuthorities(Collections.singleton("sys:role:list"));

        assertTrue(authorities.contains("sys:user:list"));
        assertFalse(authorities.contains("sys:role:list"));
    }
}
