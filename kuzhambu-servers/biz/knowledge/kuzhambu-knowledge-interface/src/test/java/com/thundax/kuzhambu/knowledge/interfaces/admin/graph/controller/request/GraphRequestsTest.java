package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class GraphRequestsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldKeepGraphIdentifiersAsStrings() throws Exception {
        var request = objectMapper.readValue(
                "{\"contentType\":\"SANCAI_ENTRY\",\"contentRefId\":\"1001\"}",
                GraphMaterialRequests.ContentRefRequest.class);

        assertThat(request.getContentRefId()).isEqualTo("1001");
        assertThat(GraphPublicationRequests.PublicationConfirmRequest.class
                        .getDeclaredField("materialLockVersion")
                        .getType())
                .isEqualTo(String.class);
        assertThat(GraphWorkbenchRequests.IncidentEdgesListRequest.class
                        .getDeclaredField("afterEdgeId")
                        .getType())
                .isEqualTo(String.class);
    }

    @Test
    void shouldRejectMissingContentReferenceAndInvalidDecision() {
        var contentRefViolations = validator.validate(new GraphMaterialRequests.ContentRefRequest());
        var decision = new GraphDeletionRequests.DeletionDecisionRequest();
        decision.setChangeId("1");
        decision.setDecision("DELETE_EVERYTHING");
        decision.setLockVersion("2");

        assertThat(contentRefViolations).isNotEmpty();
        assertThat(validator.validate(decision)).isNotEmpty();
    }

    @Test
    void shouldNotExposeOperatorFieldsInGraphRequests() {
        var requestTypes = Arrays.asList(
                GraphWorkbenchRequests.class,
                GraphMaterialRequests.class,
                GraphPublicationRequests.class,
                GraphPublishedRequests.class,
                GraphDeletionRequests.class);

        assertThat(requestTypes.stream()
                        .flatMap(type -> Arrays.stream(type.getDeclaredClasses()))
                        .flatMap(type -> Arrays.stream(type.getDeclaredFields()))
                        .map(Field::getName))
                .doesNotContain("operatorId", "publishedBy", "requestedBy");
    }
}
