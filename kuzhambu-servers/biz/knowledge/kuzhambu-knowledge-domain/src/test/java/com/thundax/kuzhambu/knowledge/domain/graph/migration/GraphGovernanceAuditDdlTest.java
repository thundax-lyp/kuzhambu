package com.thundax.kuzhambu.knowledge.domain.graph.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GraphGovernanceAuditDdlTest {

    @Test
    void schemaShouldDeclareAuditLogIdAfterReasonAndAuditIndexes() throws IOException {
        String schema = Files.readString(repoRoot().resolve("db/schema/knowledge.sql"));

        assertAuditLogIdAfterReason(schema, "knowledge_graph_governance_operation");
        assertAuditLogIdAfterReason(schema, "knowledge_graph_manual_source");
        assertAuditIndex(schema, "idx_knowledge_graph_governance_operation_audit");
        assertAuditIndex(schema, "idx_knowledge_graph_manual_source_audit");
        assertThat(tableBlock(schema, "knowledge_graph_governance_operation")).doesNotContain("operator_id");
        assertThat(tableBlock(schema, "knowledge_graph_manual_source")).doesNotContain("operator_id");
    }

    private void assertAuditLogIdAfterReason(String ddl, String tableName) {
        String table = tableBlock(ddl, tableName);
        assertThat(table.indexOf("`reason` varchar(1024) NOT NULL,"))
                .isLessThan(table.indexOf("`audit_log_id` bigint NOT NULL,"));
    }

    private void assertAuditIndex(String ddl, String indexName) {
        assertThat(ddl).contains("KEY `" + indexName + "` (`audit_log_id`)");
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
