package com.thundax.kuzhambu.system.application.audit.runtime;

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
                loaders.put(loader.objectType(), loader);
            }
        }
    }

    public AuditObjectLoader get(String objectType) {
        return loaders.get(objectType);
    }
}
