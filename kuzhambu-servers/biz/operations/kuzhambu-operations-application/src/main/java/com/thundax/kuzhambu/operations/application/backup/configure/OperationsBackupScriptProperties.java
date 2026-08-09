package com.thundax.kuzhambu.operations.application.backup.configure;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.operations.backup")
public class OperationsBackupScriptProperties {

    private String scriptsRoot = "/app/ops-scripts";
    private String backupScriptName = "backup-business-data.sh";
    private String restoreScriptName = "restore-business-data.sh";
    private String cleanupScriptName = "cleanup-backups.sh";
    private String rootPath = defaultRootPath();
    private long commandTimeoutMs = 1_800_000L;
    private boolean runPreRestore = true;
    private String postRestoreCommand = "";

    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    @Value("${spring.datasource.username}")
    private String datasourceUsername;
    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    public String getBackupRootPath() {
        return rootPath;
    }

    public void setBackupRootPath(String backupRootPath) {
        this.rootPath = backupRootPath;
    }

    private static String defaultRootPath() {
        String envPath = System.getenv("KUZHAMBU_BACKUP_ROOT_PATH");
        return StringUtils.defaultIfBlank(envPath, "/backup/kuzhambu");
    }

    public MysqlConnectionSettings resolveMysqlConnectionSettings() {
        if (datasourceUrl == null || !datasourceUrl.startsWith("jdbc:mysql://")) {
            throw new IllegalStateException(
                    "Unsupported datasource url for operations backup scripts: " + datasourceUrl);
        }
        java.net.URI uri = java.net.URI.create(datasourceUrl.substring("jdbc:".length()));
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
