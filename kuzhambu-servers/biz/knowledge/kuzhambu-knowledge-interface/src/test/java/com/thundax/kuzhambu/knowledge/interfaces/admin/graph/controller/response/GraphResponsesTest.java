package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GraphResponsesTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void shouldSerializeMaterialFailureFieldsAsExplicitNullWhenNotFailed() {
        JsonNode material = OBJECT_MAPPER.valueToTree(new GraphMaterialResponses.MaterialData(
                "2001",
                new GraphMaterialResponses.ContentRefData("SANCAI_ENTRY", "1001"),
                "三才图会卷一",
                "DRAFT",
                "3",
                null,
                null,
                null));

        assertThat(material.path("id").asText()).isEqualTo("2001");
        assertThat(material.get("publishedAt").isNull()).isTrue();
        assertThat(material.get("failureReason").isNull()).isTrue();
        assertThat(material.get("failedOperation").isNull()).isTrue();
    }

    @Test
    void shouldSerializeStringIdsAndNullableAuditFields() {
        JsonNode preview = OBJECT_MAPPER.valueToTree(new GraphPublicationResponses.PreviewData(
                "preview-token",
                new GraphMaterialResponses.ContentRefData("SANCAI_ENTRY", "1001"),
                "9007199254740993",
                List.of(new GraphPublicationResponses.MatchData("3001", "CONFLICT", "5001", "2", List.of())),
                List.of(),
                List.of(),
                false));
        JsonNode operation = OBJECT_MAPPER.valueToTree(new GraphPublishedResponses.OperationData(
                "7001", "UPDATE", "NODE", "5001", "修正名称", "8001", null, null, null, null, null));

        assertThat(preview.path("materialLockVersion").asText()).isEqualTo("9007199254740993");
        assertThat(preview.path("nodes").get(0).path("matchedObjectId").asText())
                .isEqualTo("5001");
        assertThat(operation.get("operatorId").isNull()).isTrue();
        assertThat(operation.get("occurredAt").isNull()).isTrue();
    }

    @Test
    void shouldSerializePreviewAndDeletionStructuresWithContractFieldNames() {
        JsonNode overview = OBJECT_MAPPER.valueToTree(
                new GraphWorkbenchResponses.OverviewData("12", "18", "4", "1", "2", List.of(), "3"));
        JsonNode impact = OBJECT_MAPPER.valueToTree(new GraphPublishedResponses.GovernanceImpactData(
                "impact-token", List.of(), List.of(), List.of(), List.of(), List.of(), true));
        JsonNode deletion = OBJECT_MAPPER.valueToTree(new GraphDeletionResponses.ChangeData(
                "9101",
                new GraphMaterialResponses.ContentRefData("SANCAI_ENTRY", "1001"),
                "AWAITING_DECISION",
                null,
                "4",
                Map.of("name", "张三"),
                "1720000000000",
                null));

        assertThat(overview.path("pendingConflictCount").asText()).isEqualTo("3");
        assertThat(impact.path("impactToken").asText()).isEqualTo("impact-token");
        assertThat(deletion.path("lockVersion").asText()).isEqualTo("4");
        assertThat(deletion.get("decision").isNull()).isTrue();
        assertThat(deletion.path("sourceSnapshot").path("name").asText()).isEqualTo("张三");
    }
}
