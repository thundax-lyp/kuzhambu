package com.thundax.kuzhambu.classics.application.content.support;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import java.util.Set;

public final class ClassicsContentPermissionSupport {
    public static final String CONTENT_EXPORT_PERMISSION = "classics:content:export";
    public static final String SHARING_EDIT_PERMISSION = "classics:sharing:edit";

    private ClassicsContentPermissionSupport() {}

    public static String viewPermission(ClassicsContentType contentType) {
        return switch (contentType) {
            case SANCAI_ENTRY -> "classics:sancai:view";
            case WANGQI_DOCUMENT -> "classics:wangqi:view";
            case MING_CUSTOMS -> "classics:mingcustoms:view";
        };
    }

    public static String editPermission(ClassicsContentType contentType) {
        return switch (contentType) {
            case SANCAI_ENTRY -> "classics:sancai:edit";
            case WANGQI_DOCUMENT -> "classics:wangqi:edit";
            case MING_CUSTOMS -> "classics:mingcustoms:edit";
        };
    }

    public static boolean canView(ClassicsContentType contentType, Set<String> permissions) {
        return hasPermission(permissions, viewPermission(contentType));
    }

    public static boolean canEdit(ClassicsContentType contentType, Set<String> permissions) {
        return hasPermission(permissions, editPermission(contentType));
    }

    public static boolean canExport(ClassicsContentType contentType, Set<String> permissions) {
        return canView(contentType, permissions) && hasPermission(permissions, CONTENT_EXPORT_PERMISSION);
    }

    public static boolean canShare(ClassicsContentType contentType, Set<String> permissions) {
        return canView(contentType, permissions) && hasPermission(permissions, SHARING_EDIT_PERMISSION);
    }

    private static boolean hasPermission(Set<String> permissions, String permission) {
        return permissions != null && permissions.contains(permission);
    }
}
