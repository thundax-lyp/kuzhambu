package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphGovernanceImpactToken;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphGovernanceImpactTokenPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphGovernanceImpactTokenDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphGovernanceImpactTokenMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GraphGovernanceImpactTokenRepositoryImplTest {

    @Test
    void assemblerShouldKeepFullImpactSnapshotVersionsAndMappings() {
        Instant expiresAt = Instant.parse("2026-08-14T09:00:00Z");
        GraphGovernanceImpactToken token = new GraphGovernanceImpactToken(
                "impact-token",
                "NODE_DELETE",
                "{\"nodes\":[{\"id\":11,\"lockVersion\":3}],"
                        + "\"edges\":[{\"id\":21,\"lockVersion\":4}],"
                        + "\"nodeMaterials\":[{\"publishedNodeId\":11,\"contentRefId\":1001}]}",
                expiresAt,
                null);

        GraphGovernanceImpactTokenDO dataObject = GraphGovernanceImpactTokenPersistenceAssembler.toObject(token);
        GraphGovernanceImpactToken restored = GraphGovernanceImpactTokenPersistenceAssembler.toDomain(dataObject);

        assertThat(dataObject.getSnapshotJson()).contains("\"lockVersion\":3", "\"contentRefId\":1001");
        assertThat(restored.getOperationType()).isEqualTo("NODE_DELETE");
        assertThat(restored.getSnapshotJson()).contains("\"edges\"");
        assertThat(restored.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(restored.getConsumedAt()).isNull();
    }

    @Test
    void consumeShouldAtomicallyMarkAvailableTokenAndReturnFreshRow() {
        GraphGovernanceImpactTokenMapper mapper = mock(GraphGovernanceImpactTokenMapper.class);
        GraphGovernanceImpactTokenRepositoryImpl repository = new GraphGovernanceImpactTokenRepositoryImpl(mapper);
        Instant consumedAt = Instant.parse("2026-08-14T08:05:00Z");
        when(mapper.consumeIfAvailable("impact-token", consumedAt)).thenReturn(1);
        when(mapper.selectById("impact-token"))
                .thenReturn(new GraphGovernanceImpactTokenDO(
                        "impact-token",
                        "NODE_MERGE",
                        "{\"nodes\":[{\"id\":11,\"lockVersion\":3}]}",
                        Instant.parse("2026-08-14T09:00:00Z"),
                        consumedAt));

        GraphGovernanceImpactToken consumed = repository.updateConsumedAtIfAvailable("impact-token", consumedAt);

        assertThat(consumed.getConsumedAt()).isEqualTo(consumedAt);
        assertThat(consumed.getSnapshotJson()).contains("\"lockVersion\":3");
        verify(mapper).consumeIfAvailable("impact-token", consumedAt);
    }

    @Test
    void consumeShouldReturnPreviewStaleWhenTokenExpiredOrAlreadyConsumed() {
        GraphGovernanceImpactTokenMapper mapper = mock(GraphGovernanceImpactTokenMapper.class);
        GraphGovernanceImpactTokenRepositoryImpl repository = new GraphGovernanceImpactTokenRepositoryImpl(mapper);
        Instant consumedAt = Instant.parse("2026-08-14T08:05:00Z");
        when(mapper.consumeIfAvailable("impact-token", consumedAt)).thenReturn(0);

        assertThatThrownBy(() -> repository.updateConsumedAtIfAvailable("impact-token", consumedAt))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", GraphGovernanceImpactToken.STALE_CODE);
    }
}
