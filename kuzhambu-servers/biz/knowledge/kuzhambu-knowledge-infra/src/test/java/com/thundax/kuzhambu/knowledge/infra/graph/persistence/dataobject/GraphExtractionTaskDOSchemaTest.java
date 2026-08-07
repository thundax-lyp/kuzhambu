package com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GraphExtractionTaskDOSchemaTest {

    @Test
    void schemaShouldDeclareAllTaskDataObjectColumns() throws IOException {
        String schema = Files.readString(repoRoot().resolve("db/schema/knowledge.sql"));
        String tableSchema = readCreateTableBlock(schema, "knowledge_graph_extraction_task");

        for (Field field : GraphExtractionTaskDO.class.getDeclaredFields()) {
            String columnName = toSnakeCase(field.getName());
            assertTrue(
                    tableSchema.contains("`" + columnName + "`"),
                    () -> "db/schema/knowledge.sql missing column: " + columnName);
        }
    }

    private static String readCreateTableBlock(String schema, String tableName) {
        String tableStart = "CREATE TABLE IF NOT EXISTS `" + tableName + "`";
        int start = schema.indexOf(tableStart);
        assertTrue(start >= 0, () -> "missing table: " + tableName);
        int end = schema.indexOf(";\n", start);
        assertTrue(end > start, () -> "missing table terminator: " + tableName);
        return schema.substring(start, end);
    }

    private static String toSnakeCase(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    private static Path repoRoot() {
        Path currentPath = Path.of("").toAbsolutePath();
        while (currentPath != null) {
            if (Files.exists(currentPath.resolve("db/schema/knowledge.sql"))) {
                return currentPath;
            }
            currentPath = currentPath.getParent();
        }
        throw new IllegalStateException("Cannot locate repository root");
    }
}
