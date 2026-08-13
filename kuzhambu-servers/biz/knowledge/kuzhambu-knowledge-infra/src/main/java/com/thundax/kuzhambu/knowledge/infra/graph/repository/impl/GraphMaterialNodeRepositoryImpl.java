package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialNodeRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialNodeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialNodeMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphMaterialNodeRepositoryImpl extends GraphRepositorySupport implements GraphMaterialNodeRepository {
    private final GraphMaterialNodeMapper mapper;

    public GraphMaterialNodeRepositoryImpl(GraphMaterialNodeMapper mapper, GraphMaterialMapper materialMapper) {
        super(materialMapper);
        this.mapper = mapper;
    }

    @Override
    public GraphMaterialNode getById(GraphMaterialNodeId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphMaterialNodeIdCodec.toValue(id)), null);
    }

    @Override
    public List<GraphMaterialNode> listByMaterial(ContentRef ref) {
        Long id = materialId(ref);
        if (id == null) {
            return List.of();
        }
        QueryWrapper<GraphMaterialNodeDO> w = new QueryWrapper<>();
        return mapper.selectList(w.eq("material_id", id)).stream()
                .map(v -> GraphPersistenceAssembler.toDomain(v, ref))
                .toList();
    }

    @Override
    public GraphMaterialNodeId insert(GraphMaterialNode node) {
        GraphMaterialNodeDO dataObject = GraphPersistenceAssembler.toObject(node, materialId(node.getMaterialRef()));
        mapper.insert(dataObject);
        return GraphMaterialNodeIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public void batchInsert(List<GraphMaterialNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.forEach(this::insert);
    }

    @Override
    public void batchUpdate(List<GraphMaterialNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.forEach(this::update);
    }

    @Override
    public void deleteByIds(List<GraphMaterialNodeId> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        mapper.deleteBatchIds(
                ids.stream().map(GraphMaterialNodeIdCodec::toValue).toList());
    }

    @Override
    public int update(GraphMaterialNode node) {
        return mapper.updateById(GraphPersistenceAssembler.toObject(node, materialId(node.getMaterialRef())));
    }

    @Override
    public int deleteById(GraphMaterialNodeId id) {
        return mapper.deleteById(GraphMaterialNodeIdCodec.toValue(id));
    }

    @Override
    public int deleteByMaterial(ContentRef ref) {
        Long id = materialId(ref);
        if (id == null) {
            return 0;
        }
        QueryWrapper<GraphMaterialNodeDO> w = new QueryWrapper<>();
        return mapper.delete(w.eq("material_id", id));
    }

    @Override
    public void batchReplaceByMaterial(ContentRef ref, List<GraphMaterialNode> nodes) {
        deleteByMaterial(ref);
        batchInsert(nodes);
    }
}
