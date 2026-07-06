package com.thundax.kuzhambu.operations.application.backup.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultOperationsBackupScriptExecutorTest {

    @TempDir
    private Path tempDir;

    @Test
    void executeRestoreDrillShouldPassDrillRestoreMode() throws IOException {
        Path scriptsRoot = tempDir.resolve("scripts");
        Path backupRoot = tempDir.resolve("backups");
        Path markerFile = tempDir.resolve("restore-mode.txt");
        Files.createDirectories(scriptsRoot);
        Files.createDirectories(backupRoot);
        Files.writeString(
                scriptsRoot.resolve("restore-business-data.sh"),
                """
                #!/usr/bin/env bash
                set -euo pipefail
                printf '%s' "${RESTORE_MODE}" > "$1"
                """,
                StandardCharsets.UTF_8);

        OperationsBackupScriptProperties properties = properties(scriptsRoot, backupRoot);
        DefaultOperationsBackupScriptExecutor executor = new DefaultOperationsBackupScriptExecutor(properties);

        executor.executeRestoreDrill(markerFile.toString(), "20260706-020000");

        assertEquals("DRILL", Files.readString(markerFile, StandardCharsets.UTF_8));
    }

    private OperationsBackupScriptProperties properties(Path scriptsRoot, Path backupRoot) {
        OperationsBackupScriptProperties properties = new OperationsBackupScriptProperties();
        properties.setScriptsRoot(scriptsRoot.toString());
        properties.setRestoreScriptName("restore-business-data.sh");
        properties.setBackupRootPath(backupRoot.toString());
        properties.setCommandTimeoutMs(10_000);
        properties.setRunPreRestore(true);
        properties.setPostRestoreCommand("");
        properties.setDatasourceUrl("jdbc:mysql://127.0.0.1:3306/kuzhambu");
        properties.setDatasourceUsername("root");
        properties.setDatasourcePassword("secret");
        return properties;
    }
}
