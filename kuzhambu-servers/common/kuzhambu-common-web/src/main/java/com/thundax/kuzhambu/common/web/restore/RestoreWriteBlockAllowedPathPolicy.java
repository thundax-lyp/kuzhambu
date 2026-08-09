package com.thundax.kuzhambu.common.web.restore;

import com.thundax.kuzhambu.common.web.configure.RestoreWriteBlockProperties;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

final class RestoreWriteBlockAllowedPathPolicy {

    private final Set<String> allowedPaths;

    private RestoreWriteBlockAllowedPathPolicy(Collection<String> allowedPaths) {
        this.allowedPaths = normalize(allowedPaths);
    }

    static RestoreWriteBlockAllowedPathPolicy from(RestoreWriteBlockProperties properties) {
        return new RestoreWriteBlockAllowedPathPolicy(properties == null ? null : properties.getAllowedPaths());
    }

    boolean matches(String requestUri) {
        return requestUri != null && allowedPaths.contains(requestUri);
    }

    private static Set<String> normalize(Collection<String> configuredPaths) {
        if (configuredPaths == null) {
            return Collections.emptySet();
        }
        Set<String> paths = new LinkedHashSet<>();
        for (String configuredPath : configuredPaths) {
            if (configuredPath != null) {
                paths.add(configuredPath);
            }
        }
        return Collections.unmodifiableSet(paths);
    }
}
