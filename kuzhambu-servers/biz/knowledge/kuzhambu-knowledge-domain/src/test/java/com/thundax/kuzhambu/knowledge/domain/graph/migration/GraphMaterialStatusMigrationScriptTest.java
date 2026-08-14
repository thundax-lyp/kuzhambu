package com.thundax.kuzhambu.knowledge.domain.graph.migration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GraphMaterialStatusMigrationScriptTest {

    @Test
    void schemaShouldDeclareFailureFieldsAfterPublishedAt() throws IOException {
        String schema = Files.readString(repoRoot().resolve("db/schema/knowledge.sql"));

        int publishedAt = schema.indexOf("`published_at` BIGINT DEFAULT NULL,");
        int failureReason = schema.indexOf("`failure_reason` varchar(1024) DEFAULT NULL,");
        int failedOperation = schema.indexOf("`failed_operation` varchar(16) DEFAULT NULL,");
        int extractionTask = schema.indexOf("`current_extraction_task_id` bigint DEFAULT NULL,");
        assertTrue(publishedAt >= 0);
        assertTrue(failureReason > publishedAt);
        assertTrue(failedOperation > failureReason);
        assertTrue(extractionTask > failedOperation);
    }

    @Test
    void migrationShouldAddFailureFieldsAndRewriteReadyToDraft() throws IOException {
        String migration = Files.readString(repoRoot().resolve("scripts/migrate-graph-material-status.sql"));

        assertTrue(
                migration.contains(
                        """
                ALTER TABLE knowledge_graph_material
                    ADD COLUMN failure_reason varchar(1024) DEFAULT NULL AFTER published_at,
                    ADD COLUMN failed_operation varchar(16) DEFAULT NULL AFTER failure_reason;
                """));
        assertTrue(migration.contains("UPDATE knowledge_graph_material SET status = 'DRAFT' WHERE status = 'READY';"));
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
