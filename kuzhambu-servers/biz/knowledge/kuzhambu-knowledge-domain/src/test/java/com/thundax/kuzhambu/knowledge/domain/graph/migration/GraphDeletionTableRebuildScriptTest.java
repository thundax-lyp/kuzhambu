package com.thundax.kuzhambu.knowledge.domain.graph.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GraphDeletionTableRebuildScriptTest {

    @Test
    void schemaShouldDeclareDeletionTableLockVersionsAfterStatus() throws IOException {
        String schema = Files.readString(repoRoot().resolve("db/schema/knowledge.sql"));

        assertLockVersionAfterStatus(schema, "knowledge_graph_material_deletion_change");
        assertLockVersionAfterStatus(schema, "knowledge_graph_material_deletion_task");
    }

    private void assertLockVersionAfterStatus(String ddl, String tableName) {
        String table = tableBlock(ddl, tableName);
        assertThat(table.indexOf("`status` varchar(32) NOT NULL,"))
                .isLessThan(table.indexOf("`lock_version` bigint NOT NULL DEFAULT 0,"));
    }

    private String tableBlock(String ddl, String tableName) {
        int start = ddl.indexOf("CREATE TABLE IF NOT EXISTS `" + tableName + "`");
        assertThat(start).isGreaterThanOrEqualTo(0);
        int end = ddl.indexOf(") ENGINE=InnoDB", start);
        assertThat(end).isGreaterThan(start);
        return ddl.substring(start, end);
    }

    private Path repoRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("db/schema/knowledge.sql"))) {
            current = current.getParent();
        }
        assertThat(current).isNotNull();
        return current;
    }
}
