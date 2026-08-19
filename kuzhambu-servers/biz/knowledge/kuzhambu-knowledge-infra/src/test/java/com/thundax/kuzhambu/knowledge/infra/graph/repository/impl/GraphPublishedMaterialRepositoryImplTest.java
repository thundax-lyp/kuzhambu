package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgeMaterialDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeMaterialDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedEdgeMaterialMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedNodeMaterialMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphPublishedMaterialRepositoryImplTest {

    @Test
    void insertShouldStampNodeMaterialAssociationChangeTime() {
        GraphPublishedNodeMaterialMapper mapper = mock(GraphPublishedNodeMaterialMapper.class);
        when(mapper.insert(any(GraphPublishedNodeMaterialDO.class))).thenReturn(1);
        GraphPublishedNodeMaterialRepositoryImpl repository = new GraphPublishedNodeMaterialRepositoryImpl(mapper);
        GraphPublishedNodeMaterial relation = new GraphPublishedNodeMaterial(
                new GraphPublishedNodeId(101L), new ContentRef("SANCAI_ENTRY", 1001L), "{\"name\":\"节点\"}");
        long before = System.currentTimeMillis();

        repository.insert(relation);

        ArgumentCaptor<GraphPublishedNodeMaterialDO> captor =
                ArgumentCaptor.forClass(GraphPublishedNodeMaterialDO.class);
        org.mockito.Mockito.verify(mapper).insert(captor.capture());
        assertThat(captor.getValue().getChangedAt()).isBetween(before, System.currentTimeMillis());
        assertThat(relation.getChangedAt()).isEqualTo(captor.getValue().getChangedAt());
    }

    @Test
    void insertShouldStampEdgeMaterialAssociationChangeTime() {
        GraphPublishedEdgeMaterialMapper mapper = mock(GraphPublishedEdgeMaterialMapper.class);
        when(mapper.insert(any(GraphPublishedEdgeMaterialDO.class))).thenReturn(1);
        GraphPublishedEdgeMaterialRepositoryImpl repository = new GraphPublishedEdgeMaterialRepositoryImpl(mapper);
        GraphPublishedEdgeMaterial relation = new GraphPublishedEdgeMaterial(
                new GraphPublishedEdgeId(201L), new ContentRef("SANCAI_ENTRY", 1001L), "{\"name\":\"关系\"}");
        long before = System.currentTimeMillis();

        repository.insert(relation);

        ArgumentCaptor<GraphPublishedEdgeMaterialDO> captor =
                ArgumentCaptor.forClass(GraphPublishedEdgeMaterialDO.class);
        org.mockito.Mockito.verify(mapper).insert(captor.capture());
        assertThat(captor.getValue().getChangedAt()).isBetween(before, System.currentTimeMillis());
        assertThat(relation.getChangedAt()).isEqualTo(captor.getValue().getChangedAt());
    }
}
