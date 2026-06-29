package com.thundax.kuzhambu.operations.application.backup.support;

import java.net.URI;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class OperationsBackupScriptProperties {

    @Value("${kuzhambu.operations.backup.scripts-root:/app/ops-scripts}")
    private String scriptsRoot;

    @Value("${kuzhambu.operations.backup.backup-script-name:backup-business-data.sh}")
    private String backupScriptName;

    @Value("${kuzhambu.operations.backup.restore-script-name:restore-business-data.sh}")
    private String restoreScriptName;

    @Value("${kuzhambu.operations.backup.cleanup-script-name:cleanup-backups.sh}")
    private String cleanupScriptName;

    @Value("${kuzhambu.operations.backup.root-path:${KUZHAMBU_BACKUP_ROOT_PATH:/backup/kuzhambu}}")
    private String backupRootPath;

    @Value("${kuzhambu.operations.backup.command-timeout-ms:1800000}")
    private long commandTimeoutMs;

    @Value("${kuzhambu.operations.backup.run-pre-restore:true}")
    private boolean runPreRestore;

    @Value("${kuzhambu.operations.backup.post-restore-command:}")
    private String postRestoreCommand;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    public MysqlConnectionSettings resolveMysqlConnectionSettings() {
        if (datasourceUrl == null || !datasourceUrl.startsWith("jdbc:mysql://")) {
            throw new IllegalStateException(
                    "Unsupported datasource url for operations backup scripts: " + datasourceUrl);
        }
        URI uri = URI.create(datasourceUrl.substring("jdbc:".length()));
        String databaseName = uri.getPath();
        if (databaseName == null || databaseName.isBlank() || "/".equals(databaseName)) {
            throw new IllegalStateException("Missing database name in datasource url: " + datasourceUrl);
        }
        return new MysqlConnectionSettings(
                uri.getHost(),
                uri.getPort() > 0 ? String.valueOf(uri.getPort()) : "3306",
                databaseName.startsWith("/") ? databaseName.substring(1) : databaseName,
                datasourceUsername,
                datasourcePassword == null ? "" : datasourcePassword);
    }

    public record MysqlConnectionSettings(
            String host, String port, String databaseName, String username, String password) {}
}
