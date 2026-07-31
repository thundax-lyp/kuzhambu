package com.thundax.kuzhambu.system.infra.auth.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.common.cache.KuzhambuCacheNames;
import java.lang.reflect.Field;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuthCacheNamespaceTest {

    @Test
    void javaTimeRepositoriesShouldUseInstantCacheContracts() throws Exception {
        assertCacheContract(
                PrincipalAccessTokenRepositoryImpl.class, "PRINCIPAL_ACCESS_TOKEN_", "issuedAt", "expireAt");
        assertCacheContract(
                PrincipalAuthSessionRepositoryImpl.class,
                "PRINCIPAL_AUTH_SESSION_",
                "issuedAt",
                "lastAccessTime",
                "expireAt");
        assertCacheContract(
                PrincipalRefreshTokenRepositoryImpl.class, "PRINCIPAL_REFRESH_TOKEN_", "issuedAt", "expireAt");
    }

    private void assertCacheContract(Class<?> repositoryType, String cacheSuffix, String... timeFields)
            throws Exception {
        Field cacheSection = repositoryType.getDeclaredField("CACHE_SECTION");
        cacheSection.setAccessible(true);
        assertEquals(KuzhambuCacheNames.PREFIX + cacheSuffix, cacheSection.get(null));

        Class<?> cacheDtoType = Class.forName(
                repositoryType.getName() + "$" + repositoryType.getSimpleName().replace("RepositoryImpl", "CacheDTO"));
        for (String timeField : timeFields) {
            assertEquals(Instant.class, cacheDtoType.getDeclaredField(timeField).getType());
        }
    }
}
