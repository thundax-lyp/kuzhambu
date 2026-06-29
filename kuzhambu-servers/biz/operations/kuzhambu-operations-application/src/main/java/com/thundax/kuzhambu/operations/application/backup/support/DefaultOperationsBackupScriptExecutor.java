package com.thundax.kuzhambu.operations.application.backup.support;

import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupScriptProperties.MysqlConnectionSettings;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupSupportModels.OperationsBackupArtifactResult;
import com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DefaultOperationsBackupScriptExecutor implements OperationsBackupScriptExecutor {

    private final OperationsBackupScriptProperties properties;

    public DefaultOperationsBackupScriptExecutor(OperationsBackupScriptProperties properties) {
        this.properties = properties;
    }

    @Override
    public OperationsBackupArtifactResult executeBackup(BackupType backupType, String timestamp) {
        if (backupType == null) {
            throw new IllegalArgumentException("Operations backup type must not be null.");
        }
        if (StringUtils.isBlank(timestamp)) {
            throw new IllegalArgumentException("Operations backup timestamp must not be blank.");
        }
        runScript(
                Path.of(properties.getScriptsRoot(), properties.getBackupScriptName()),
                Map.of(
                        "BACKUP_TYPE", backupType.value(),
                        "BACKUP_PREFIX", backupType.filePrefix(),
                        "TIMESTAMP", timestamp));
        return loadArtifact(backupType.filePrefix() + "_" + timestamp);
    }

    @Override
    public void executeRestore(String backupBaseName, String preRestoreTimestamp) {
        if (StringUtils.isBlank(backupBaseName)) {
            throw new IllegalArgumentException("Operations restore backupBaseName must not be blank.");
        }
        if (StringUtils.isBlank(preRestoreTimestamp)) {
            throw new IllegalArgumentException("Operations restore preRestoreTimestamp must not be blank.");
        }
        runScript(
                Path.of(properties.getScriptsRoot(), properties.getRestoreScriptName()),
                List.of(backupBaseName),
                Map.of(
                        "KUZHAMBU_RESTORE_ALLOW",
                        "YES",
                        "RUN_PRE_RESTORE",
                        String.valueOf(properties.isRunPreRestore()),
                        "PRE_RESTORE_TIMESTAMP",
                        preRestoreTimestamp,
                        "POST_RESTORE_COMMAND",
                        StringUtils.defaultString(properties.getPostRestoreCommand())));
    }

    @Override
    public OperationsBackupArtifactResult loadArtifact(String baseName) {
        if (StringUtils.isBlank(baseName)) {
            throw new IllegalArgumentException("Operations backup baseName must not be blank.");
        }
        Path backupRoot = Path.of(properties.getBackupRootPath());
        Path sqlFile = backupRoot.resolve(baseName + ".sql");
        Path sqlChecksumFile = backupRoot.resolve(baseName + ".sql.sha256");
        Path storageArchive = backupRoot.resolve(baseName + ".storage.tar.gz");
        Path storageChecksumFile = backupRoot.resolve(baseName + ".storage.tar.gz.sha256");
        if (!Files.exists(sqlFile)) {
            throw new IllegalStateException("Operations backup sql artifact not found: " + sqlFile);
        }
        if (!Files.exists(sqlChecksumFile)) {
            throw new IllegalStateException("Operations backup checksum artifact not found: " + sqlChecksumFile);
        }
        if (!Files.exists(storageArchive)) {
            throw new IllegalStateException("Operations backup storage archive not found: " + storageArchive);
        }
        if (!Files.exists(storageChecksumFile)) {
            throw new IllegalStateException("Operations backup storage checksum not found: " + storageChecksumFile);
        }
        try {
            return new OperationsBackupArtifactResult(
                    baseName,
                    sqlFile.getFileName().toString(),
                    sqlFile,
                    Files.size(sqlFile),
                    readChecksum(sqlChecksumFile),
                    storageArchive.getFileName().toString(),
                    readChecksum(storageChecksumFile));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect operations backup artifact: " + baseName, exception);
        }
    }

    private void runScript(Path scriptPath, Map<String, String> extraEnvironment) {
        runScript(scriptPath, List.of(), extraEnvironment);
    }

    private void runScript(Path scriptPath, List<String> arguments, Map<String, String> extraEnvironment) {
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Operations backup script not found: " + scriptPath);
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(buildCommand(scriptPath, arguments));
        processBuilder.directory(scriptPath.getParent().toFile());
        processBuilder.redirectErrorStream(true);
        Map<String, String> environment = processBuilder.environment();
        MysqlConnectionSettings mysqlSettings = properties.resolveMysqlConnectionSettings();
        environment.put("KUZHAMBU_DB_HOST", mysqlSettings.host());
        environment.put("KUZHAMBU_DB_PORT", mysqlSettings.port());
        environment.put("KUZHAMBU_DB_NAME", mysqlSettings.databaseName());
        environment.put("KUZHAMBU_DB_USERNAME", mysqlSettings.username());
        environment.put("KUZHAMBU_DB_PASSWORD", mysqlSettings.password());
        environment.put("KUZHAMBU_BACKUP_ROOT_PATH", properties.getBackupRootPath());
        environment.putAll(extraEnvironment);
        String output = "";
        try {
            Process process = processBuilder.start();
            boolean finished = process.waitFor(properties.getCommandTimeoutMs(), TimeUnit.MILLISECONDS);
            output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Operations backup script timed out: " + scriptPath.getFileName());
            }
            if (process.exitValue() != 0) {
                throw new IllegalStateException("Operations backup script failed: " + sanitizeOutput(output));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start operations backup script: " + scriptPath, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operations backup script execution interrupted: " + scriptPath, exception);
        }
    }

    private List<String> buildCommand(Path scriptPath, List<String> arguments) {
        List<String> command = new java.util.ArrayList<>();
        command.add("bash");
        command.add(scriptPath.toString());
        command.addAll(arguments);
        return command;
    }

    private String readChecksum(Path checksumFile) throws IOException {
        String line = Files.readString(checksumFile, StandardCharsets.UTF_8).trim();
        if (line.isBlank()) {
            throw new IllegalStateException("Operations backup checksum file is empty: " + checksumFile);
        }
        return line.split("\\s+")[0];
    }

    private String sanitizeOutput(String output) {
        if (StringUtils.isBlank(output)) {
            return "script exited with non-zero status";
        }
        String compact = output.replace('\r', '\n').trim().replaceAll("\\n{2,}", "\n");
        return compact.length() > 1000 ? compact.substring(0, 1000) : compact;
    }
}
