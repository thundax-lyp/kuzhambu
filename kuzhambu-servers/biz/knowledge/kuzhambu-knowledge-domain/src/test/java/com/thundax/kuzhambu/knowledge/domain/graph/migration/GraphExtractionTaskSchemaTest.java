package com.thundax.kuzhambu.knowledge.domain.graph.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GraphExtractionTaskSchemaTest {

    @Test
    void schemaShouldDeclareMaterialStatsSnapshot() throws IOException {
        String schema = schema();

        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS `knowledge_graph_material_stats`"));
        assertTrue(schema.contains("`material_id` bigint NOT NULL,"));
        assertTrue(schema.contains("`stats_revision` bigint NOT NULL DEFAULT 0,"));
        assertTrue(schema.contains("PRIMARY KEY (`material_id`)"));
    }

    @Test
    void schemaShouldSeparateTaskExecutionAndDisposition() throws IOException {
        String schema = schema();

        assertTrue(schema.contains("`execution_status` varchar(32) NOT NULL DEFAULT 'PENDING',"));
        assertTrue(schema.contains("`disposition` varchar(32) DEFAULT NULL,"));
        assertTrue(schema.contains("`attempt_no` int NOT NULL DEFAULT 0,"));
        assertTrue(schema.contains("`lock_version` bigint NOT NULL DEFAULT 0,"));
        assertTrue(schema.contains("`purge_after` BIGINT DEFAULT NULL,"));
        assertThat(schema).doesNotContain("`retry_from_task_id` bigint DEFAULT NULL,");
        assertThat(schema).doesNotContain("KEY `idx_knowledge_graph_extraction_task_material_status`");
        assertThat(schema).doesNotContain("CREATE TABLE IF NOT EXISTS `knowledge_graph_extraction_stage`");
    }

    @Test
    void schemaShouldPreventConcurrentActiveTasksForOneMaterial() throws IOException {
        String schema = schema();

        assertTrue(schema.contains(
                "CASE WHEN `execution_status` IN ('PENDING', 'RUNNING') THEN `material_id` ELSE NULL END"));
        assertTrue(schema.contains(
                "UNIQUE KEY `uk_knowledge_graph_extraction_task_active_material` (`active_task_material_id`)"));
    }

    private static String schema() throws IOException {
        return Files.readString(repoRoot().resolve("db/schema/knowledge.sql"));
    }

    private static Path repoRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("db/schema/knowledge.sql"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Repository root not found");
    }
}
