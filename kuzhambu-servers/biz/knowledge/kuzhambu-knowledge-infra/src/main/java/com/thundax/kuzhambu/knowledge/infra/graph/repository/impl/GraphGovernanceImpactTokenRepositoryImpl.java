package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphGovernanceImpactToken;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphGovernanceImpactTokenRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphGovernanceImpactTokenPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphGovernanceImpactTokenMapper;
import java.time.Instant;
import org.springframework.stereotype.Repository;

@Repository
public class GraphGovernanceImpactTokenRepositoryImpl implements GraphGovernanceImpactTokenRepository {
    private final GraphGovernanceImpactTokenMapper mapper;

    public GraphGovernanceImpactTokenRepositoryImpl(GraphGovernanceImpactTokenMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphGovernanceImpactToken getByToken(String token) {
        return GraphGovernanceImpactTokenPersistenceAssembler.toDomain(mapper.selectById(token));
    }

    @Override
    public String insert(GraphGovernanceImpactToken token) {
        var dataObject = GraphGovernanceImpactTokenPersistenceAssembler.toObject(token);
        mapper.insert(dataObject);
        return dataObject.getToken();
    }

    @Override
    public GraphGovernanceImpactToken updateConsumedAtIfAvailable(String token, Instant consumedAt) {
        int updated = mapper.consumeIfAvailable(token, consumedAt);
        if (updated != 1) {
            throw GraphGovernanceImpactToken.stale();
        }
        return getByToken(token);
    }
}
