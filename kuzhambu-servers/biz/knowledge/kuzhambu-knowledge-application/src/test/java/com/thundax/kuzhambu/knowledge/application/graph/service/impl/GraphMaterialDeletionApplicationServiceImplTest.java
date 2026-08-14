package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionDecisionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionPrecheckCommand;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionChange;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionDecision;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionChangeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialDeletionChangeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialDeletionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class GraphMaterialDeletionApplicationServiceImplTest {

    @Test
    void precheckShouldPersistSnapshotWithPublishedSources() {
        Fixture fixture = new Fixture();
        when(fixture.materialRepository.getByContentRef(fixture.ref)).thenReturn(material(fixture.ref));
        when(fixture.nodeMaterialRepository.listByMaterial(fixture.ref))
                .thenReturn(List.of(new GraphPublishedNodeMaterial(
                        new GraphPublishedNodeId(10L), fixture.ref, "{\"node\":\"snapshot\"}")));
        when(fixture.edgeMaterialRepository.listByMaterial(fixture.ref))
                .thenReturn(List.of(new GraphPublishedEdgeMaterial(
                        new GraphPublishedEdgeId(20L), fixture.ref, "{\"edge\":\"snapshot\"}")));

        fixture.service.precheck(new GraphMaterialDeletionPrecheckCommand(fixture.ref));

        verify(fixture.changeRepository)
                .insert(org.mockito.ArgumentMatchers.argThat(
                        change -> change.getStatus() == GraphMaterialDeletionStatus.AWAITING_DECISION
                                && change.getMaterialSnapshotJson().contains("\\\"node\\\":\\\"snapshot\\\"")
                                && change.getMaterialSnapshotJson().contains("\\\"edge\\\":\\\"snapshot\\\"")));
    }

    @Test
    void preserveDecisionShouldReadSourceSnapshotsBeforeRemovingMaterialRefs() {
        Fixture fixture = new Fixture();
        GraphMaterialDeletionChange change = change(fixture.ref, 4L);
        when(fixture.changeRepository.getById(change.getId())).thenReturn(change);
        when(fixture.changeRepository.updateIfLockVersion(any(), org.mockito.ArgumentMatchers.eq(4L)))
                .thenReturn(change);

        fixture.service.decide(new GraphMaterialDeletionDecisionCommand(
                change.getId(), GraphMaterialDeletionDecision.PRESERVE_CONTRIBUTION, 4L));

        InOrder order = inOrder(fixture.nodeMaterialRepository, fixture.edgeMaterialRepository);
        order.verify(fixture.nodeMaterialRepository).listByMaterial(fixture.ref);
        order.verify(fixture.edgeMaterialRepository).listByMaterial(fixture.ref);
        order.verify(fixture.nodeMaterialRepository).deleteByMaterial(fixture.ref);
        order.verify(fixture.edgeMaterialRepository).deleteByMaterial(fixture.ref);
    }

    @Test
    void withdrawDecisionShouldOnlyRemoveCurrentMaterialAssociations() {
        Fixture fixture = new Fixture();
        GraphMaterialDeletionChange change = change(fixture.ref, 4L);
        when(fixture.changeRepository.getById(change.getId())).thenReturn(change);
        when(fixture.changeRepository.updateIfLockVersion(any(), org.mockito.ArgumentMatchers.eq(4L)))
                .thenReturn(change);

        fixture.service.decide(new GraphMaterialDeletionDecisionCommand(
                change.getId(), GraphMaterialDeletionDecision.WITHDRAW_ASSOCIATIONS, 4L));

        verify(fixture.nodeMaterialRepository).deleteByMaterial(fixture.ref);
        verify(fixture.edgeMaterialRepository).deleteByMaterial(fixture.ref);
    }

    @Test
    void decisionShouldReturnGraphLockConflictWhenVersionIsStale() {
        Fixture fixture = new Fixture();
        GraphMaterialDeletionChange change = change(fixture.ref, 5L);
        when(fixture.changeRepository.getById(change.getId())).thenReturn(change);

        assertThatThrownBy(() -> fixture.service.decide(new GraphMaterialDeletionDecisionCommand(
                        change.getId(), GraphMaterialDeletionDecision.WITHDRAW_ASSOCIATIONS, 4L)))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(GraphMaterialDeletionChange.LOCK_CONFLICT_CODE);
    }

    private GraphMaterial material(ContentRef ref) {
        return new GraphMaterial(ref, "三才图会", GraphMaterialStatus.PUBLISHED, Instant.parse("2026-08-14T00:00:00Z"), 0L);
    }

    private GraphMaterialDeletionChange change(ContentRef ref, long lockVersion) {
        return new GraphMaterialDeletionChange(
                new GraphMaterialDeletionChangeId(1001L),
                ref.getContentId(),
                ref,
                "{}",
                null,
                GraphMaterialDeletionStatus.AWAITING_DECISION,
                lockVersion,
                null,
                Instant.parse("2026-08-14T00:00:00Z"),
                null);
    }

    private static final class Fixture {
        private final ContentRef ref = new ContentRef("SANCAI_ENTRY", 3001L);
        private final GraphMaterialRepository materialRepository = mock(GraphMaterialRepository.class);
        private final GraphMaterialDeletionChangeRepository changeRepository =
                mock(GraphMaterialDeletionChangeRepository.class);
        private final GraphMaterialDeletionTaskRepository taskRepository =
                mock(GraphMaterialDeletionTaskRepository.class);
        private final GraphPublishedNodeMaterialRepository nodeMaterialRepository =
                mock(GraphPublishedNodeMaterialRepository.class);
        private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository =
                mock(GraphPublishedEdgeMaterialRepository.class);
        private final GraphMaterialDeletionApplicationServiceImpl service =
                new GraphMaterialDeletionApplicationServiceImpl(
                        materialRepository,
                        changeRepository,
                        taskRepository,
                        nodeMaterialRepository,
                        edgeMaterialRepository,
                        Clock.fixed(Instant.parse("2026-08-14T00:00:00Z"), ZoneOffset.UTC));
    }
}
