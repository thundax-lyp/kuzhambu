package com.thundax.kuzhambu.operations.application.cleanup.support;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public final class OperationsCleanupSupport {

    public static final String CLEANUP_TYPE_EXPIRED_BACKUP = "EXPIRED_BACKUP";
    public static final String CLEANUP_TYPE_EXPIRED_SHARE = "EXPIRED_SHARE";
    public static final String CLEANUP_TYPE_EXPIRED_DRAFT = "EXPIRED_DRAFT";
    public static final String CLEANUP_TYPE_EXPIRED_EXPORT = "EXPIRED_EXPORT";
    public static final String CLEANUP_TYPE_EXPIRED_REPORT = "EXPIRED_REPORT";
    public static final String CLEANUP_TYPE_EXPIRED_HEALTH_CHECK = "EXPIRED_HEALTH_CHECK";
    public static final String CLEANUP_TYPE_EXPIRED_LONG_TASK = "EXPIRED_LONG_TASK";

    public static final String CLEANUP_ITEM_TYPE_BACKUP = "backup";
    public static final String CLEANUP_ITEM_TYPE_SHARE = "share";
    public static final String CLEANUP_ITEM_TYPE_DRAFT = "draft";
    public static final String CLEANUP_ITEM_TYPE_EXPORT = "export";
    public static final String CLEANUP_ITEM_TYPE_REPORT = "report";
    public static final String CLEANUP_ITEM_TYPE_HEALTH_CHECK = "health-check";
    public static final String CLEANUP_ITEM_TYPE_LONG_TASK = "long-task";

    public static final String CLEANUP_STATUS_RUNNING = "RUNNING";
    public static final String CLEANUP_STATUS_SUCCEEDED = "SUCCEEDED";
    public static final String CLEANUP_STATUS_FAILED = "FAILED";

    public static final String CLEANUP_ITEM_STATUS_SUCCEEDED = "SUCCEEDED";
    public static final String CLEANUP_ITEM_STATUS_FAILED = "FAILED";

    public static final int DEFAULT_CLEANUP_TARGET_LIMIT = 200;

    private static final List<String> ORDERED_CLEANUP_TYPES = List.of(
            CLEANUP_TYPE_EXPIRED_BACKUP,
            CLEANUP_TYPE_EXPIRED_EXPORT,
            CLEANUP_TYPE_EXPIRED_SHARE,
            CLEANUP_TYPE_EXPIRED_DRAFT,
            CLEANUP_TYPE_EXPIRED_REPORT,
            CLEANUP_TYPE_EXPIRED_HEALTH_CHECK,
            CLEANUP_TYPE_EXPIRED_LONG_TASK);

    private static final Set<String> SUPPORTED_CLEANUP_TYPES = Set.copyOf(ORDERED_CLEANUP_TYPES);

    private OperationsCleanupSupport() {}

    public static boolean isSupportedType(String cleanupType) {
        return SUPPORTED_CLEANUP_TYPES.contains(normalizeType(cleanupType));
    }

    public static String normalizeType(String cleanupType) {
        return cleanupType == null ? null : cleanupType.trim().toUpperCase(Locale.ROOT);
    }

    public static List<String> orderedCleanupTypes() {
        return ORDERED_CLEANUP_TYPES;
    }

    public static String resolveItemType(String normalizedCleanupType) {
        if (StringUtils.isBlank(normalizedCleanupType)) {
            return "unknown";
        }
        return switch (normalizedCleanupType) {
            case CLEANUP_TYPE_EXPIRED_BACKUP -> CLEANUP_ITEM_TYPE_BACKUP;
            case CLEANUP_TYPE_EXPIRED_SHARE -> CLEANUP_ITEM_TYPE_SHARE;
            case CLEANUP_TYPE_EXPIRED_DRAFT -> CLEANUP_ITEM_TYPE_DRAFT;
            case CLEANUP_TYPE_EXPIRED_EXPORT -> CLEANUP_ITEM_TYPE_EXPORT;
            case CLEANUP_TYPE_EXPIRED_REPORT -> CLEANUP_ITEM_TYPE_REPORT;
            case CLEANUP_TYPE_EXPIRED_HEALTH_CHECK -> CLEANUP_ITEM_TYPE_HEALTH_CHECK;
            case CLEANUP_TYPE_EXPIRED_LONG_TASK -> CLEANUP_ITEM_TYPE_LONG_TASK;
            default -> "unknown";
        };
    }
}
