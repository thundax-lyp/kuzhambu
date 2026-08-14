package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphGovernanceOperation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphGovernanceOperationId;
import java.util.List;

public interface GraphGovernanceOperationRepository {

    GraphGovernanceOperation getById(GraphGovernanceOperationId id);

    List<GraphGovernanceOperation> listByTarget(String targetType, Long targetId);

    GraphGovernanceOperationId insert(GraphGovernanceOperation operation);
}
