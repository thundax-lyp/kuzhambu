package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEdgeRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialEdgeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialEdgeMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphMaterialEdgeRepositoryImpl extends GraphRepositorySupport implements GraphMaterialEdgeRepository {
    private final GraphMaterialEdgeMapper mapper;

    public GraphMaterialEdgeRepositoryImpl(GraphMaterialEdgeMapper mapper, GraphMaterialMapper materialMapper) {
        super(materialMapper);
        this.mapper = mapper;
    }

    @Override
    public GraphMaterialEdge getById(GraphMaterialEdgeId id) {
        GraphMaterialEdgeDO dataObject = mapper.selectById(GraphMaterialEdgeIdCodec.toValue(id));
        return GraphPersistenceAssembler.toDomain(
                dataObject, dataObject == null ? null : materialRef(dataObject.getMaterialId()));
    }

    @Override
    public List<GraphMaterialEdge> listByMaterial(ContentRef ref) {
        Long id = materialId(ref);
        if (id == null) {
            return List.of();
        }
        QueryWrapper<GraphMaterialEdgeDO> w = new QueryWrapper<>();
        return mapper.selectList(w.eq("material_id", id)).stream()
                .map(v -> GraphPersistenceAssembler.toDomain(v, ref))
                .toList();
    }

    @Override
    public GraphMaterialEdgeId insert(GraphMaterialEdge edge) {
        GraphMaterialEdgeDO dataObject = GraphPersistenceAssembler.toObject(edge, materialId(edge.getMaterialRef()));
        mapper.insert(dataObject);
        return GraphMaterialEdgeIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public void batchInsert(List<GraphMaterialEdge> edges) {
        if (edges == null || edges.isEmpty()) {
            return;
        }
        edges.forEach(this::insert);
    }

    @Override
    public void batchUpdate(List<GraphMaterialEdge> edges) {
        if (edges == null || edges.isEmpty()) {
            return;
        }
        edges.forEach(this::update);
    }

    @Override
    public void batchDeleteById(List<GraphMaterialEdgeId> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        mapper.deleteBatchIds(
                ids.stream().map(GraphMaterialEdgeIdCodec::toValue).toList());
    }

    @Override
    public int update(GraphMaterialEdge edge) {
        return mapper.updateById(GraphPersistenceAssembler.toObject(edge, materialId(edge.getMaterialRef())));
    }

    @Override
    public int deleteById(GraphMaterialEdgeId id) {
        return mapper.deleteById(GraphMaterialEdgeIdCodec.toValue(id));
    }

    @Override
    public int deleteByMaterial(ContentRef ref) {
        Long id = materialId(ref);
        if (id == null) {
            return 0;
        }
        QueryWrapper<GraphMaterialEdgeDO> w = new QueryWrapper<>();
        return mapper.delete(w.eq("material_id", id));
    }

    @Override
    public void batchReplaceByMaterial(ContentRef ref, List<GraphMaterialEdge> edges) {
        deleteByMaterial(ref);
        batchInsert(edges);
    }
}
