package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishRecord;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishRecordRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPublishRecordPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishRecordDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishRecordMapper;
import org.springframework.stereotype.Repository;

@Repository
public class GraphPublishRecordRepositoryImpl extends GraphRepositorySupport implements GraphPublishRecordRepository {
    private final GraphPublishRecordMapper mapper;

    public GraphPublishRecordRepositoryImpl(GraphPublishRecordMapper mapper, GraphMaterialMapper materialMapper) {
        super(materialMapper);
        this.mapper = mapper;
    }

    @Override
    public Long insert(GraphPublishRecord record) {
        GraphPublishRecordDO dataObject =
                GraphPublishRecordPersistenceAssembler.toObject(record, materialId(record.getMaterialRef()));
        mapper.insert(dataObject);
        return dataObject.getId();
    }
}
