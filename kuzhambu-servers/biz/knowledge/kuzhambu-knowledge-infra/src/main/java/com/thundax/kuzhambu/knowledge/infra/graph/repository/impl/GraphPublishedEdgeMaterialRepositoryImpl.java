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
import org.springframework.dao.DuplicateKeyException;
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
        try {
            return mapper.insert(GraphPersistenceAssembler.toObject(relation));
        } catch (DuplicateKeyException ex) {
            if (exists(relation)) {
                return 0;
            }
            throw ex;
        }
    }

    @Override
    public void batchInsert(List<GraphPublishedEdgeMaterial> relations) {
        if (relations == null || relations.isEmpty()) {
            return;
        }
        relations.forEach(this::insert);
    }

    @Override
    public int deleteByPublishedEdgeId(GraphPublishedEdgeId id) {
        QueryWrapper<GraphPublishedEdgeMaterialDO> w = new QueryWrapper<>();
        return mapper.delete(w.eq("published_edge_id", GraphPublishedEdgeIdCodec.toValue(id)));
    }

    @Override
    public int deleteByPublishedEdgeIds(List<GraphPublishedEdgeId> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        QueryWrapper<GraphPublishedEdgeMaterialDO> w = new QueryWrapper<>();
        return mapper.delete(w.in(
                "published_edge_id",
                ids.stream().map(GraphPublishedEdgeIdCodec::toValue).toList()));
    }

    @Override
    public int deleteByPublishedEdgeIdAndMaterialRef(GraphPublishedEdgeId id, ContentRef ref) {
        return mapper.delete(byMaterial(ref).eq("published_edge_id", GraphPublishedEdgeIdCodec.toValue(id)));
    }

    @Override
    public int deleteByMaterial(ContentRef ref) {
        return mapper.delete(byMaterial(ref));
    }

    @Override
    public long countDistinctMaterials() {
        return mapper.countDistinctMaterials();
    }

    private boolean exists(GraphPublishedEdgeMaterial relation) {
        return mapper.selectCount(byMaterial(relation.getMaterialRef())
                        .eq("published_edge_id", GraphPublishedEdgeIdCodec.toValue(relation.getPublishedEdgeId())))
                > 0;
    }

    private QueryWrapper<GraphPublishedEdgeMaterialDO> byMaterial(ContentRef ref) {
        QueryWrapper<GraphPublishedEdgeMaterialDO> w = new QueryWrapper<>();
        return w.eq("content_type", ContentRefCodec.toContentType(ref))
                .eq("content_ref_id", ContentRefCodec.toValue(ref));
    }
}
