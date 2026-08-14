package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublicationPreviewToken;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublicationPreviewTokenRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPublicationPreviewTokenPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublicationPreviewTokenDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublicationPreviewTokenMapper;
import java.time.Instant;
import org.springframework.stereotype.Repository;

@Repository
public class GraphPublicationPreviewTokenRepositoryImpl extends GraphRepositorySupport
        implements GraphPublicationPreviewTokenRepository {
    private final GraphPublicationPreviewTokenMapper mapper;

    public GraphPublicationPreviewTokenRepositoryImpl(
            GraphPublicationPreviewTokenMapper mapper, GraphMaterialMapper materialMapper) {
        super(materialMapper);
        this.mapper = mapper;
    }

    @Override
    public GraphPublicationPreviewToken getByToken(String token) {
        GraphPublicationPreviewTokenDO dataObject = mapper.selectById(token);
        return GraphPublicationPreviewTokenPersistenceAssembler.toDomain(
                dataObject, dataObject == null ? null : materialRef(dataObject.getMaterialId()));
    }

    @Override
    public String insert(GraphPublicationPreviewToken token) {
        GraphPublicationPreviewTokenDO dataObject =
                GraphPublicationPreviewTokenPersistenceAssembler.toObject(token, materialId(token.getMaterialRef()));
        mapper.insert(dataObject);
        return dataObject.getToken();
    }

    @Override
    public GraphPublicationPreviewToken updateConsumedAtIfAvailable(String token, Instant consumedAt) {
        int updated = mapper.consumeIfAvailable(token, consumedAt);
        if (updated != 1) {
            throw GraphPublicationPreviewToken.stale();
        }
        return getByToken(token);
    }
}
