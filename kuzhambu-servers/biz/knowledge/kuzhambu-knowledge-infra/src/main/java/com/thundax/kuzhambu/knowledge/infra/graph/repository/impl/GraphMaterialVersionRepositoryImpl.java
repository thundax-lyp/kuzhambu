package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialVersionRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialVersionDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialVersionMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphMaterialVersionRepositoryImpl extends GraphRepositorySupport
        implements GraphMaterialVersionRepository {
    private final GraphMaterialVersionMapper mapper;

    public GraphMaterialVersionRepositoryImpl(GraphMaterialVersionMapper mapper, GraphMaterialMapper materialMapper) {
        super(materialMapper);
        this.mapper = mapper;
    }

    @Override
    public List<GraphMaterialVersion> listByMaterial(ContentRef ref) {
        Long id = materialId(ref);
        if (id == null) {
            return List.of();
        }
        QueryWrapper<GraphMaterialVersionDO> w = new QueryWrapper<>();
        return mapper.selectList(w.eq("material_id", id).orderByDesc("version_no")).stream()
                .map(v -> GraphPersistenceAssembler.toDomain(v, ref))
                .toList();
    }

    @Override
    public GraphMaterialVersion getByMaterialAndVersionNo(ContentRef ref, long versionNo) {
        Long id = materialId(ref);
        if (id == null) {
            return null;
        }
        QueryWrapper<GraphMaterialVersionDO> w = new QueryWrapper<>();
        return GraphPersistenceAssembler.toDomain(
                mapper.selectOne(w.eq("material_id", id).eq("version_no", versionNo)), ref);
    }

    @Override
    public int insert(GraphMaterialVersion version) {
        return mapper.insert(GraphPersistenceAssembler.toObject(version, materialId(version.getMaterialRef())));
    }
}
