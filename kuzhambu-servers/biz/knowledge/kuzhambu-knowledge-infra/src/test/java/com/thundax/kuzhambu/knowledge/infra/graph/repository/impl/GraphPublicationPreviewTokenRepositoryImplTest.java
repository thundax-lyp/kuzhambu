package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublicationPreviewToken;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPublicationPreviewTokenPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublicationPreviewTokenDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublicationPreviewTokenMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GraphPublicationPreviewTokenRepositoryImplTest {

    @Test
    void assemblerShouldMapSnapshotVersionsAndExpiry() {
        Instant expiresAt = Instant.parse("2026-08-14T09:00:00Z");
        GraphPublicationPreviewToken token = new GraphPublicationPreviewToken(
                "preview-token",
                new ContentRef("SANCAI_ENTRY", 1001L),
                7L,
                "{\"nodes\":[{\"id\":11,\"lockVersion\":3}]}",
                expiresAt,
                null);

        GraphPublicationPreviewTokenDO dataObject =
                GraphPublicationPreviewTokenPersistenceAssembler.toObject(token, 1001L);
        GraphPublicationPreviewToken restored = GraphPublicationPreviewTokenPersistenceAssembler.toDomain(
                dataObject, new ContentRef("SANCAI_ENTRY", 1001L));

        assertThat(dataObject.getSnapshotJson()).contains("\"lockVersion\":3");
        assertThat(restored.getMaterialRef()).isEqualTo(new ContentRef("SANCAI_ENTRY", 1001L));
        assertThat(restored.getMaterialLockVersion()).isEqualTo(7L);
        assertThat(restored.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(restored.getConsumedAt()).isNull();
    }

    @Test
    void consumeShouldAtomicallyMarkAvailableTokenAndReturnFreshRow() {
        GraphPublicationPreviewTokenMapper mapper = mock(GraphPublicationPreviewTokenMapper.class);
        GraphMaterialMapper materialMapper = mock(GraphMaterialMapper.class);
        GraphPublicationPreviewTokenRepositoryImpl repository =
                new GraphPublicationPreviewTokenRepositoryImpl(mapper, materialMapper);
        Instant consumedAt = Instant.parse("2026-08-14T08:05:00Z");
        when(mapper.consumeIfAvailable("preview-token", consumedAt)).thenReturn(1);
        when(mapper.selectById("preview-token"))
                .thenReturn(new GraphPublicationPreviewTokenDO(
                        "preview-token",
                        1001L,
                        7L,
                        "{\"edges\":[{\"id\":21,\"lockVersion\":4}]}",
                        Instant.parse("2026-08-14T09:00:00Z"),
                        consumedAt));

        GraphPublicationPreviewToken consumed = repository.updateConsumedAtIfAvailable("preview-token", consumedAt);

        assertThat(consumed.getConsumedAt()).isEqualTo(consumedAt);
        assertThat(consumed.getSnapshotJson()).contains("\"lockVersion\":4");
        verify(mapper).consumeIfAvailable("preview-token", consumedAt);
    }

    @Test
    void consumeShouldReturnPreviewStaleWhenTokenExpiredOrAlreadyConsumed() {
        GraphPublicationPreviewTokenMapper mapper = mock(GraphPublicationPreviewTokenMapper.class);
        GraphMaterialMapper materialMapper = mock(GraphMaterialMapper.class);
        GraphPublicationPreviewTokenRepositoryImpl repository =
                new GraphPublicationPreviewTokenRepositoryImpl(mapper, materialMapper);
        Instant consumedAt = Instant.parse("2026-08-14T08:05:00Z");
        when(mapper.consumeIfAvailable("preview-token", consumedAt)).thenReturn(0);

        assertThatThrownBy(() -> repository.updateConsumedAtIfAvailable("preview-token", consumedAt))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", GraphPublicationPreviewToken.STALE_CODE);
    }
}
