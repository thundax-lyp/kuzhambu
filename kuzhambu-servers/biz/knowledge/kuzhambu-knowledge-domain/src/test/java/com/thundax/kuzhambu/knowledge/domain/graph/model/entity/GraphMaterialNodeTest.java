package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import org.junit.jupiter.api.Test;

class GraphMaterialNodeTest {

    @Test
    void refreshNodeKeyShouldGenerateStableBusinessKey() {
        GraphMaterialNode first = node("李白");
        GraphMaterialNode second = node("\t李白 ");

        first.refreshNodeKey("唐");
        second.refreshNodeKey(" 唐 ");

        assertNotNull(first.getNodeKey());
        assertEquals(first.getNodeKey(), second.getNodeKey());
        assertTrue(first.sameBusinessKey(second));
    }

    @Test
    void requireMaterialShouldRejectDifferentMaterial() {
        GraphMaterialNode node = node("李白");

        assertThrows(DomainException.class, () -> node.requireMaterial(new ContentRef("SANCAI_ENTRY", 2L)));
    }

    @Test
    void validateRequiredFieldsShouldRejectBlankName() {
        GraphMaterialNode node = node(" ");

        assertThrows(DomainException.class, node::validateRequiredFields);
    }

    private static GraphMaterialNode node(String name) {
        GraphMaterialNode node = new GraphMaterialNode();
        node.setMaterialRef(new ContentRef("SANCAI_ENTRY", 1L));
        node.setNodeType(GraphNodeType.PERSON);
        node.setName(name);
        node.setSource(GraphSourceType.MANUAL);
        return node;
    }
}
