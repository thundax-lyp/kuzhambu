package com.thundax.kuzhambu.knowledge.domain.graph.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphEdgeKeyCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphNodeKeyCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GraphKeyHelperTest {

    @Test
    void shouldGenerateSameNodeKeyForEquivalentNormalizedValues() {
        assertThat(GraphNodeKeyCodec.toDomain(GraphNodeType.PERSON, " 李 白 ", "唐"))
                .isEqualTo(GraphNodeKeyCodec.toDomain(GraphNodeType.PERSON, "李　白", " 唐 "));
    }

    @Test
    void shouldGenerateSameUndirectedEdgeKeyForReversedEndpoints() {
        GraphNodeKey first = GraphNodeKeyCodec.toDomain(GraphNodeType.PERSON, "李白", null);
        GraphNodeKey second = GraphNodeKeyCodec.toDomain(GraphNodeType.PERSON, "杜甫", null);

        assertThat(GraphEdgeKeyCodec.toDomain(first, second, "ASSOCIATED_WITH", false, Map.of("role", "友人")))
                .isEqualTo(GraphEdgeKeyCodec.toDomain(second, first, "ASSOCIATED_WITH", false, Map.of("role", "友人")));
    }
}
