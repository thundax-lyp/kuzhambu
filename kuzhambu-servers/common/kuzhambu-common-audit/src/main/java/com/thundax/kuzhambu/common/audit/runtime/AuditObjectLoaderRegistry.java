package com.thundax.kuzhambu.common.audit.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AuditObjectLoaderRegistry {

    private final Map<String, AuditObjectLoader> loaders = new HashMap<>();

    public AuditObjectLoaderRegistry(List<AuditObjectLoader> loaderList) {
        if (loaderList != null) {
            for (AuditObjectLoader loader : loaderList) {
                String objectType = loader.objectType();
                if (!hasText(objectType)) {
                    throw new IllegalStateException("Audit object loader objectType must not be blank.");
                }
                AuditObjectLoader previous = loaders.putIfAbsent(objectType, loader);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate audit object loader objectType: " + objectType);
                }
            }
        }
    }

    public AuditObjectLoader get(String objectType) {
        return loaders.get(objectType);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
