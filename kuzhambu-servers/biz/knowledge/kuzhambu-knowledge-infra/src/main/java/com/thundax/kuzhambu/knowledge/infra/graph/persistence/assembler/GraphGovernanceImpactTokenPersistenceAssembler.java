package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphGovernanceImpactToken;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphGovernanceImpactTokenDO;

public final class GraphGovernanceImpactTokenPersistenceAssembler {
    private GraphGovernanceImpactTokenPersistenceAssembler() {}

    public static GraphGovernanceImpactTokenDO toObject(GraphGovernanceImpactToken token) {
        if (token == null) {
            return null;
        }
        return new GraphGovernanceImpactTokenDO(
                token.getToken(),
                token.getOperationType(),
                token.getSnapshotJson(),
                token.getExpiresAt(),
                token.getConsumedAt());
    }

    public static GraphGovernanceImpactToken toDomain(GraphGovernanceImpactTokenDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphGovernanceImpactToken(
                dataObject.getToken(),
                dataObject.getOperationType(),
                dataObject.getSnapshotJson(),
                dataObject.getExpiresAt(),
                dataObject.getConsumedAt());
    }
}
