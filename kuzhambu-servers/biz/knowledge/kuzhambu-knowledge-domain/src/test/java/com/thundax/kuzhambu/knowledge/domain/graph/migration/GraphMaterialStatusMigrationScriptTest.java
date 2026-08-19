package com.thundax.kuzhambu.knowledge.domain.graph.migration;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(publishedAt).isGreaterThanOrEqualTo(0);
        assertThat(failureReason).isGreaterThan(publishedAt);
        assertThat(failedOperation).isGreaterThan(failureReason);
        assertThat(extractionTask).isGreaterThan(failedOperation);
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
