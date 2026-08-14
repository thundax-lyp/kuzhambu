package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialDeletionChangeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionChange;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionChangeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialDeletionChangeRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDeletionChangeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialDeletionChangeMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphMaterialDeletionChangeRepositoryImpl implements GraphMaterialDeletionChangeRepository {
    private final GraphMaterialDeletionChangeMapper mapper;

    public GraphMaterialDeletionChangeRepositoryImpl(GraphMaterialDeletionChangeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphMaterialDeletionChange getById(GraphMaterialDeletionChangeId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphMaterialDeletionChangeIdCodec.toValue(id)));
    }

    @Override
    public GraphMaterialDeletionChange getByLatestMaterialRef(ContentRef materialRef) {
        QueryWrapper<GraphMaterialDeletionChangeDO> w = new QueryWrapper<>();
        return GraphPersistenceAssembler.toDomain(
                mapper.selectOne(w.eq("content_type", ContentRefCodec.toContentType(materialRef))
                        .eq("content_ref_id", ContentRefCodec.toValue(materialRef))
                        .orderByDesc("requested_at")
                        .orderByDesc("id")
                        .last("limit 1")));
    }

    @Override
    public PageResult<GraphMaterialDeletionChange> page(GraphMaterialDeletionStatus status, int pageNo, int pageSize) {
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;
        long total = mapper.selectCount(changeQuery(status));
        QueryWrapper<GraphMaterialDeletionChangeDO> pageWrapper = changeQuery(status)
                .orderByDesc("requested_at")
                .orderByDesc("id")
                .last("limit " + offset(effectivePageNo, effectivePageSize) + ", " + effectivePageSize);
        List<GraphMaterialDeletionChange> records = mapper.selectList(pageWrapper).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
        return PageResult.of(effectivePageNo, effectivePageSize, total, records);
    }

    @Override
    public GraphMaterialDeletionChangeId insert(GraphMaterialDeletionChange change) {
        GraphMaterialDeletionChangeDO dataObject = GraphPersistenceAssembler.toObject(change);
        mapper.insert(dataObject);
        return GraphMaterialDeletionChangeIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public GraphMaterialDeletionChange updateIfLockVersion(
            GraphMaterialDeletionChange change, long expectedLockVersion) {
        int updated = mapper.updateIfLockVersion(GraphPersistenceAssembler.toObject(change), expectedLockVersion);
        if (updated != 1) {
            throw GraphMaterialDeletionChange.lockConflict();
        }
        return getById(change.getId());
    }

    private QueryWrapper<GraphMaterialDeletionChangeDO> changeQuery(GraphMaterialDeletionStatus status) {
        QueryWrapper<GraphMaterialDeletionChangeDO> w = new QueryWrapper<>();
        if (status != null) {
            w.eq("status", status.value());
        }
        return w;
    }

    private int offset(int pageNo, int pageSize) {
        return (pageNo - 1) * pageSize;
    }
}
