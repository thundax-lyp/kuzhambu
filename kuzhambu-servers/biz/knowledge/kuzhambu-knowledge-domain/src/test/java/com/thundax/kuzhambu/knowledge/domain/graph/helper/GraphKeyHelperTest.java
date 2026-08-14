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

    @Test
    void shouldKeepWhitespaceWordBoundariesAndQualifierBoundariesDistinct() {
        GraphNodeKey newYork = GraphNodeKeyCodec.toDomain(GraphNodeType.PLACE, "New York", null);
        GraphNodeKey newYorkWithoutSpace = GraphNodeKeyCodec.toDomain(GraphNodeType.PLACE, "NewYork", null);

        assertThat(newYork).isNotEqualTo(newYorkWithoutSpace);
        assertThat(GraphEdgeKeyCodec.toDomain(
                        newYork, newYorkWithoutSpace, "ASSOCIATED_WITH", true, Map.of("role", "a&time=b")))
                .isNotEqualTo(GraphEdgeKeyCodec.toDomain(
                        newYork, newYorkWithoutSpace, "ASSOCIATED_WITH", true, Map.of("role", "a", "time", "b")));
    }

    @Test
    void shouldIncludeSchemaKeyFieldsInNodeKey() {
        assertThat(GraphKeyHelper.generateNodeKey(GraphNodeType.PLACE, "长安", Map.of("placeKind", "CITY")))
                .isNotEqualTo(GraphKeyHelper.generateNodeKey(GraphNodeType.PLACE, "长安", Map.of("placeKind", "COUNTY")));
    }
}
