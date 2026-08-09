package com.thundax.kuzhambu.operations.application.backup.support;

import com.thundax.kuzhambu.operations.application.backup.configure.OperationsBackupScriptProperties;

public final class OperationsMysqlConnectionSettingsResolver {

    private OperationsMysqlConnectionSettingsResolver() {}

    public static MysqlConnectionSettings resolve(OperationsBackupScriptProperties properties) {
        String datasourceUrl = properties.getDatasourceUrl();
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
                properties.getDatasourceUsername(),
                properties.getDatasourcePassword() == null ? "" : properties.getDatasourcePassword());
    }

    public record MysqlConnectionSettings(
            String host, String port, String databaseName, String username, String password) {}
}
