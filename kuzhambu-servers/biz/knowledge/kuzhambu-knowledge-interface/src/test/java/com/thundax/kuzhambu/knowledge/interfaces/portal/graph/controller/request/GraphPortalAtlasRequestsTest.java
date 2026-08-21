package com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphPortalAtlasRequestsTest {
    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptLongMaximumGraphIdentifiers() {
        GraphPortalAtlasRequests.OneHopEdgesListRequest request = request("9223372036854775807");
        request.setAfterEdgeId("9223372036854775807");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void shouldRejectGraphIdentifiersAboveLongMaximum() {
        GraphPortalAtlasRequests.OneHopEdgesListRequest request = request("9223372036854775808");
        request.setAfterEdgeId("999999999999999999999999");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("nodeIds[0].<list element>", "afterEdgeId");
    }

    private static GraphPortalAtlasRequests.OneHopEdgesListRequest request(String nodeId) {
        GraphPortalAtlasRequests.OneHopEdgesListRequest request = new GraphPortalAtlasRequests.OneHopEdgesListRequest();
        request.setNodeIds(List.of(nodeId));
        return request;
    }
}
