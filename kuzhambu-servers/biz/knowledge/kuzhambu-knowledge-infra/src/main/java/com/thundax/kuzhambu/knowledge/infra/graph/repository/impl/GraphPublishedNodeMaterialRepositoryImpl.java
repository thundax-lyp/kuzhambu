package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeMaterialDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedNodeMaterialMapper;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
public class GraphPublishedNodeMaterialRepositoryImpl implements GraphPublishedNodeMaterialRepository {
    private final GraphPublishedNodeMaterialMapper mapper;

    public GraphPublishedNodeMaterialRepositoryImpl(GraphPublishedNodeMaterialMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<GraphPublishedNodeMaterial> listByPublishedNodeId(GraphPublishedNodeId id) {
        QueryWrapper<GraphPublishedNodeMaterialDO> w = new QueryWrapper<>();
        return mapper.selectList(w.eq("published_node_id", GraphPublishedNodeIdCodec.toValue(id))).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<GraphPublishedNodeMaterial> listByMaterial(ContentRef ref) {
        return mapper.selectList(byMaterial(ref)).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public int insert(GraphPublishedNodeMaterial relation) {
        relation.setChangedAt(System.currentTimeMillis());
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
    public void batchInsert(List<GraphPublishedNodeMaterial> relations) {
        if (relations == null || relations.isEmpty()) {
            return;
        }
        relations.forEach(this::insert);
    }

    @Override
    public int deleteByPublishedNodeId(GraphPublishedNodeId id) {
        QueryWrapper<GraphPublishedNodeMaterialDO> w = new QueryWrapper<>();
        return mapper.delete(w.eq("published_node_id", GraphPublishedNodeIdCodec.toValue(id)));
    }

    @Override
    public int deleteByPublishedNodeIds(List<GraphPublishedNodeId> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        QueryWrapper<GraphPublishedNodeMaterialDO> w = new QueryWrapper<>();
        return mapper.delete(w.in(
                "published_node_id",
                ids.stream().map(GraphPublishedNodeIdCodec::toValue).toList()));
    }

    @Override
    public int deleteByPublishedNodeIdAndMaterialRef(GraphPublishedNodeId id, ContentRef ref) {
        return mapper.delete(byMaterial(ref).eq("published_node_id", GraphPublishedNodeIdCodec.toValue(id)));
    }

    @Override
    public int deleteByMaterial(ContentRef ref) {
        return mapper.delete(byMaterial(ref));
    }

    @Override
    public long countDistinctMaterials() {
        return mapper.countDistinctMaterials();
    }

    private boolean exists(GraphPublishedNodeMaterial relation) {
        return mapper.selectCount(byMaterial(relation.getMaterialRef())
                        .eq("published_node_id", GraphPublishedNodeIdCodec.toValue(relation.getPublishedNodeId())))
                > 0;
    }

    private QueryWrapper<GraphPublishedNodeMaterialDO> byMaterial(ContentRef ref) {
        QueryWrapper<GraphPublishedNodeMaterialDO> w = new QueryWrapper<>();
        return w.eq("content_type", ContentRefCodec.toContentType(ref))
                .eq("content_ref_id", ContentRefCodec.toValue(ref));
    }
}
