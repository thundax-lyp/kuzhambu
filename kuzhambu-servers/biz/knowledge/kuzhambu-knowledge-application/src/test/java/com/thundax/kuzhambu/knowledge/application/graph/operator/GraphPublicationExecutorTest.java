package com.thundax.kuzhambu.knowledge.application.graph.operator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublicationCommand;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublicationPreviewToken;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublicationPreviewTokenRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishRecordRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgePropertyRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodePropertyRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

class GraphPublicationExecutorTest {

    @Test
    void publishShouldRequireDecisionForEachPreviewConflict() {
        Fixture fixture = new Fixture();
        GraphMaterial material = new GraphMaterial(fixture.ref, "三才图会", GraphMaterialStatus.READY, null, 0L);
        GraphMaterial statusMaterial = new GraphMaterial(fixture.ref, "三才图会", GraphMaterialStatus.READY, null, 0L);
        GraphMaterialNode materialNode = new GraphMaterialNode(
                new GraphMaterialNodeId(11L),
                fixture.ref,
                fixture.nodeKey,
                GraphNodeType.PERSON,
                "张三",
                GraphSourceType.AI,
                "{}");
        GraphPublishedNode publishedNode = new GraphPublishedNode(
                new GraphPublishedNodeId(21L),
                fixture.nodeKey,
                GraphNodeType.PERSON,
                "张三",
                GraphSourceType.MANUAL,
                GraphPublishedStatus.ACTIVE,
                Instant.parse("2026-08-14T00:00:00Z"),
                2L);
        when(fixture.graphLoader.require(fixture.ref))
                .thenReturn(GraphMaterialGraph.of(material, List.of(materialNode), List.of()));
        when(fixture.materialRepository.getByContentRef(fixture.ref)).thenReturn(statusMaterial);
        when(fixture.materialRepository.updateIfLockVersion(statusMaterial, 0L)).thenReturn(1);
        when(fixture.nodeRepository.getByNodeKey(fixture.nodeKey)).thenReturn(publishedNode);
        when(fixture.previewTokenRepository.getByToken("preview-token"))
                .thenReturn(new GraphPublicationPreviewToken(
                        "preview-token",
                        fixture.ref,
                        0L,
                        "{\"materialLockVersion\":0,\"nodes\":[{\"materialObjectId\":11,"
                                + "\"matchType\":\"CONFLICT\",\"matchedObjectId\":21,"
                                + "\"matchedObjectLockVersion\":2}],\"edges\":[]}",
                        Instant.now().plusSeconds(60),
                        null));

        assertThatThrownBy(() -> fixture.executor.publishOne(
                        new GraphPublicationCommand(fixture.ref, 0L, 9001L, "preview-token", List.of())))
                .isInstanceOf(DomainException.class)
                .extracting("code")
                .isEqualTo(GraphPublicationPreviewToken.STALE_CODE);
        verify(fixture.previewTokenRepository, never())
                .updateConsumedAtIfAvailable(org.mockito.Mockito.any(), org.mockito.Mockito.any());
    }

    private static final class Fixture {
        private final ContentRef ref = new ContentRef("SANCAI_ENTRY", 1001L);
        private final GraphNodeKey nodeKey = new GraphNodeKey("person:zhang-san");
        private final GraphMaterialGraphLoader graphLoader = mock(GraphMaterialGraphLoader.class);
        private final GraphSnapshotResolver snapshotResolver = mock(GraphSnapshotResolver.class);
        private final GraphSchemaResolver schemaResolver = mock(GraphSchemaResolver.class);
        private final GraphMaterialRepository materialRepository = mock(GraphMaterialRepository.class);
        private final GraphMaterialVersionRepository versionRepository = mock(GraphMaterialVersionRepository.class);
        private final GraphPublishedNodeRepository nodeRepository = mock(GraphPublishedNodeRepository.class);
        private final GraphPublishedEdgeRepository edgeRepository = mock(GraphPublishedEdgeRepository.class);
        private final GraphPublishedNodePropertyRepository nodePropertyRepository =
                mock(GraphPublishedNodePropertyRepository.class);
        private final GraphPublishedEdgePropertyRepository edgePropertyRepository =
                mock(GraphPublishedEdgePropertyRepository.class);
        private final GraphPublishedNodeMaterialRepository nodeMaterialRepository =
                mock(GraphPublishedNodeMaterialRepository.class);
        private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository =
                mock(GraphPublishedEdgeMaterialRepository.class);
        private final GraphPublishRecordRepository publishRecordRepository = mock(GraphPublishRecordRepository.class);
        private final GraphPublicationPreviewTokenRepository previewTokenRepository =
                mock(GraphPublicationPreviewTokenRepository.class);
        private final GraphPublicationExecutor executor = new GraphPublicationExecutor(
                new ObjectMapper(),
                graphLoader,
                snapshotResolver,
                schemaResolver,
                materialRepository,
                versionRepository,
                nodeRepository,
                edgeRepository,
                nodePropertyRepository,
                edgePropertyRepository,
                nodeMaterialRepository,
                edgeMaterialRepository,
                publishRecordRepository,
                previewTokenRepository,
                new NoopTransactionManager());
    }

    private static final class NoopTransactionManager extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {}

        @Override
        protected void doCommit(DefaultTransactionStatus status) {}

        @Override
        protected void doRollback(DefaultTransactionStatus status) {}
    }
}
