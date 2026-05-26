package com.thundax.kuzhambu.common.test.integration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public class IntegrationDatabaseScriptRunner {

    private final SqlScriptExecutor scriptExecutor;

    public IntegrationDatabaseScriptRunner(DataSource dataSource) {
        this(new JdbcSqlScriptExecutor(dataSource));
    }

    IntegrationDatabaseScriptRunner(SqlScriptExecutor scriptExecutor) {
        this.scriptExecutor = scriptExecutor;
    }

    public List<Path> runDirectories(Path... directories) {
        List<Path> executed = new ArrayList<Path>();
        if (directories == null) {
            return executed;
        }
        for (Path directory : directories) {
            executed.addAll(runDirectory(directory));
        }
        return executed;
    }

    public List<Path> runDirectory(Path directory) {
        List<Path> scripts = scripts(directory);
        for (Path script : scripts) {
            scriptExecutor.execute(script);
        }
        return scripts;
    }

    public List<Path> scripts(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            return Collections.emptyList();
        }
        try {
            List<Path> scripts = new ArrayList<Path>();
            Files.list(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .sorted()
                    .forEach(scripts::add);
            return scripts;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list integration SQL scripts: " + directory, e);
        }
    }

    interface SqlScriptExecutor {
        void execute(Path script);
    }

    private static class JdbcSqlScriptExecutor implements SqlScriptExecutor {
        private final DataSource dataSource;

        JdbcSqlScriptExecutor(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public void execute(Path script) {
            Connection connection = DataSourceUtils.getConnection(dataSource);
            try {
                ScriptUtils.executeSqlScript(
                        connection,
                        new EncodedResource(new FileSystemResource(script.toFile()), StandardCharsets.UTF_8));
            } finally {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }
}
