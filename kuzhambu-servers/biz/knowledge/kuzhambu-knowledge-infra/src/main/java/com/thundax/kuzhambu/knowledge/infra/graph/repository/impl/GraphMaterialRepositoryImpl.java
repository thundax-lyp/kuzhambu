package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialMapper;
import java.util.List;
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
    public List<GraphMaterial> listByContentRefs(List<ContentRef> contentRefs) {
        if (contentRefs == null || contentRefs.isEmpty()) {
            return List.of();
        }
        List<GraphMaterialDO> refs = contentRefs.stream()
                .map(ref -> new GraphMaterialDO(
                        null,
                        ContentRefCodec.toContentType(ref),
                        ContentRefCodec.toValue(ref),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                .toList();
        return mapper.selectByRefs(refs).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public PageResult<GraphMaterial> page(String keyword, GraphMaterialStatus status, int pageNo, int pageSize) {
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;
        String statusValue = status == null ? null : status.value();
        long total = mapper.countMaterials(keyword, statusValue);
        List<GraphMaterial> records = mapper
                .pageMaterials(keyword, statusValue, (effectivePageNo - 1) * effectivePageSize, effectivePageSize)
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
        return PageResult.of(effectivePageNo, effectivePageSize, total, records);
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
    public int updateIfLockVersion(GraphMaterial material, long expectedLockVersion) {
        return mapper.updateIfLockVersion(GraphPersistenceAssembler.toObject(material), expectedLockVersion);
    }

    @Override
    public int deleteByContentRef(ContentRef ref) {
        return mapper.delete(byRef(ref));
    }
}
