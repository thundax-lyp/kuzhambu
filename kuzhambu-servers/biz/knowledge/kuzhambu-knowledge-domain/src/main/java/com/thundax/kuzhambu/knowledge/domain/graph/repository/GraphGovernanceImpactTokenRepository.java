package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphGovernanceImpactToken;
import java.time.Instant;

public interface GraphGovernanceImpactTokenRepository {
    GraphGovernanceImpactToken getByToken(String token);

    String insert(GraphGovernanceImpactToken token);

    GraphGovernanceImpactToken updateConsumedAtIfAvailable(String token, Instant consumedAt);
}
