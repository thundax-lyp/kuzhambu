package com.thundax.kuzhambu.common.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class IntegrationDatabaseScriptRunnerTest {

    @TempDir
    private Path tempDir;

    @Test
    public void shouldListSqlScriptsInNameOrder() throws IOException {
        Files.writeString(tempDir.resolve("002_data.sql"), "select 2;");
        Files.writeString(tempDir.resolve("001_schema.sql"), "select 1;");
        Files.writeString(tempDir.resolve("notes.txt"), "ignored");
        IntegrationDatabaseScriptRunner runner = new IntegrationDatabaseScriptRunner(script -> {});

        List<Path> scripts = runner.scripts(tempDir);

        assertEquals(List.of(tempDir.resolve("001_schema.sql"), tempDir.resolve("002_data.sql")), scripts);
    }
}
