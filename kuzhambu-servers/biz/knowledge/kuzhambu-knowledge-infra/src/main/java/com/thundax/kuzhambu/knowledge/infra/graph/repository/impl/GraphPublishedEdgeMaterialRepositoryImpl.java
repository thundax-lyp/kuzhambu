package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgeMaterialDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedEdgeMaterialMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphPublishedEdgeMaterialRepositoryImpl implements GraphPublishedEdgeMaterialRepository {
    private final GraphPublishedEdgeMaterialMapper mapper;

    public GraphPublishedEdgeMaterialRepositoryImpl(GraphPublishedEdgeMaterialMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<GraphPublishedEdgeMaterial> listByPublishedEdgeId(GraphPublishedEdgeId id) {
        QueryWrapper<GraphPublishedEdgeMaterialDO> w = new QueryWrapper<>();
        return mapper.selectList(w.eq("published_edge_id", GraphPublishedEdgeIdCodec.toValue(id))).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<GraphPublishedEdgeMaterial> listByMaterial(ContentRef ref) {
        return mapper.selectList(byMaterial(ref)).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public int insert(GraphPublishedEdgeMaterial relation) {
        return mapper.insert(GraphPersistenceAssembler.toObject(relation));
    }

    @Override
    public int deleteByPublishedEdgeIdAndMaterialRef(GraphPublishedEdgeId id, ContentRef ref) {
        return mapper.delete(byMaterial(ref).eq("published_edge_id", GraphPublishedEdgeIdCodec.toValue(id)));
    }

    @Override
    public int deleteByMaterial(ContentRef ref) {
        return mapper.delete(byMaterial(ref));
    }

    private QueryWrapper<GraphPublishedEdgeMaterialDO> byMaterial(ContentRef ref) {
        QueryWrapper<GraphPublishedEdgeMaterialDO> w = new QueryWrapper<>();
        return w.eq("content_type", ContentRefCodec.toContentType(ref))
                .eq("content_ref_id", ContentRefCodec.toValue(ref));
    }
}
