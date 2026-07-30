package com.thundax.kuzhambu.system.infra.auth.repository.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class AuthCacheNamespaceTest {

    @Test
    void javaTimeCacheDtosShouldUseVersionedNamespaces() throws Exception {
        assertVersioned(PrincipalAccessTokenRepositoryImpl.class);
        assertVersioned(PrincipalAuthSessionRepositoryImpl.class);
        assertVersioned(PrincipalRefreshTokenRepositoryImpl.class);
    }

    private void assertVersioned(Class<?> repositoryType) throws Exception {
        Field cacheSection = repositoryType.getDeclaredField("CACHE_SECTION");
        cacheSection.setAccessible(true);
        assertTrue(String.valueOf(cacheSection.get(null)).contains("_V2_"));
    }
}
