package com.thundax.kuzhambu.knowledge.domain.graph.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GraphGovernanceImpactTokenDdlTest {

    @Test
    void schemaShouldDeclareGovernanceImpactTokenSnapshotAndExpiryIndex() throws IOException {
        String schema = Files.readString(repoRoot().resolve("db/schema/knowledge.sql"));
        String table = tableBlock(schema, "knowledge_graph_governance_impact_token");

        assertThat(table).contains("`token` varchar(64) NOT NULL");
        assertThat(table).contains("`operation_type` varchar(32) NOT NULL");
        assertThat(table).contains("`snapshot_json` json NOT NULL");
        assertThat(table).contains("`expires_at` BIGINT NOT NULL");
        assertThat(table).contains("`consumed_at` BIGINT DEFAULT NULL");
        assertThat(table)
                .contains("KEY `idx_knowledge_graph_governance_impact_expiry` (`operation_type`, `expires_at`)");
        assertThat(table).doesNotContain("snapshot_hash");
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
