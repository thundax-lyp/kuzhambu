package com.thundax.kuzhambu.operations.application.health.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class OperationsHealthRecoveryLinkFactory {

    public static final String ACTION_OPEN_HEALTH_DETAIL = "OPEN_HEALTH_DETAIL";
    public static final String ACTION_OPEN_BACKUP_RESTORE = "OPEN_BACKUP_RESTORE";
    public static final String ACTION_OPEN_CLEANUP_DETAIL = "OPEN_CLEANUP_DETAIL";
    public static final String ACTION_OPEN_TASK_DETAIL = "OPEN_TASK_DETAIL";
    public static final String ACTION_RUN_MANUAL_BACKUP = "RUN_MANUAL_BACKUP";
    public static final String ACTION_NONE = "NONE";

    public String healthDetailTarget(String component) {
        return "{\"route\":\"/operations/dashboard\",\"component\":\"" + escapeJson(component) + "\"}";
    }

    public String backupRestoreTarget(Long backupId, Long restoreId, String action) {
        StringBuilder builder = new StringBuilder("{\"route\":\"/operations/backup-restore\"");
        appendLongField(builder, "backupId", backupId);
        appendLongField(builder, "restoreId", restoreId);
        appendStringField(builder, "action", action);
        return builder.append("}").toString();
    }

    public String cleanupTarget(Long cleanupId) {
        return "{\"route\":\"/operations/cleanup\",\"cleanupId\":" + cleanupId + "}";
    }

    public String taskTarget(Long snapshotId) {
        return "{\"route\":\"/operations/tasks\",\"snapshotId\":" + snapshotId + "}";
    }

    private static void appendLongField(StringBuilder builder, String fieldName, Long value) {
        if (value != null) {
            builder.append(",\"").append(fieldName).append("\":").append(value);
        }
    }

    private static void appendStringField(StringBuilder builder, String fieldName, String value) {
        if (StringUtils.isNotBlank(value)) {
            builder.append(",\"")
                    .append(fieldName)
                    .append("\":\"")
                    .append(escapeJson(value))
                    .append("\"");
        }
    }

    private static String escapeJson(String value) {
        return StringUtils.defaultString(value).replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
