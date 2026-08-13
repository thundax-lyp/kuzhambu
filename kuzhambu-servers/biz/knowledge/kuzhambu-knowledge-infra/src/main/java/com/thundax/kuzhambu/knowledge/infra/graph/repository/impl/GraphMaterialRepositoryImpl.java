package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialMapper;
import org.springframework.stereotype.Repository;

@Repository
public class GraphMaterialRepositoryImpl implements GraphMaterialRepository {
    private final GraphMaterialMapper mapper;

    public GraphMaterialRepositoryImpl(GraphMaterialMapper mapper) {
        this.mapper = mapper;
    }

    private QueryWrapper<GraphMaterialDO> byRef(ContentRef ref) {
        QueryWrapper<GraphMaterialDO> w = new QueryWrapper<>();
        return w.eq("content_type", ContentRefCodec.toContentType(ref))
                .eq("content_ref_id", ContentRefCodec.toValue(ref));
    }

    @Override
    public GraphMaterial getByContentRef(ContentRef ref) {
        return GraphPersistenceAssembler.toDomain(mapper.selectOne(byRef(ref)));
    }

    @Override
    public int insert(GraphMaterial material) {
        return mapper.insert(GraphPersistenceAssembler.toObject(material));
    }

    @Override
    public int update(GraphMaterial material) {
        return mapper.update(GraphPersistenceAssembler.toObject(material), byRef(material.getContentRef()));
    }

    @Override
    public int deleteByContentRef(ContentRef ref) {
        return mapper.delete(byRef(ref));
    }
}
