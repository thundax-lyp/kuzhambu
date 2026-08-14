package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialEventIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEventRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialEventDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialEventMapper;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
public class GraphMaterialEventRepositoryImpl implements GraphMaterialEventRepository {
    private final GraphMaterialEventMapper mapper;

    public GraphMaterialEventRepositoryImpl(GraphMaterialEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphMaterialEvent getById(GraphMaterialEventId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphMaterialEventIdCodec.toValue(id)));
    }

    @Override
    public GraphMaterialEvent getByMaterialRefAndType(ContentRef ref, GraphMaterialEventType type) {
        return GraphPersistenceAssembler.toDomain(mapper.selectOne(byMaterialRefAndType(ref, type)));
    }

    @Override
    public PageResult<GraphMaterialEvent> page(
            ContentRef ref, GraphMaterialEventType type, GraphMaterialEventStatus status, int pageNo, int pageSize) {
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;
        long total = mapper.selectCount(eventQuery(ref, type, status));
        QueryWrapper<GraphMaterialEventDO> pageWrapper = eventQuery(ref, type, status)
                .orderByDesc("changed_at")
                .orderByDesc("id")
                .last("limit " + offset(effectivePageNo, effectivePageSize) + ", " + effectivePageSize);
        List<GraphMaterialEvent> records = mapper.selectList(pageWrapper).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
        return PageResult.of(effectivePageNo, effectivePageSize, total, records);
    }

    @Override
    public List<GraphMaterialEvent> listByStatus(GraphMaterialEventStatus status, int limit) {
        QueryWrapper<GraphMaterialEventDO> w = new QueryWrapper<>();
        int effectiveLimit = limit <= 0 ? 20 : limit;
        return mapper
                .selectList(w.eq("status", status.value())
                        .orderByAsc("changed_at")
                        .orderByAsc("id")
                        .last("limit " + effectiveLimit))
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public GraphMaterialEventId insert(GraphMaterialEvent event) {
        GraphMaterialEventDO dataObject = GraphPersistenceAssembler.toObject(event);
        try {
            mapper.insert(dataObject);
            return GraphMaterialEventIdCodec.toDomain(dataObject.getId());
        } catch (DuplicateKeyException ex) {
            GraphMaterialEvent existing = getByMaterialRefAndType(event.getMaterialRef(), event.getType());
            if (existing != null) {
                return existing.getId();
            }
            throw ex;
        }
    }

    @Override
    public int updateIfLockVersion(GraphMaterialEvent event, long expectedLockVersion) {
        return mapper.updateIfLockVersion(GraphPersistenceAssembler.toObject(event), expectedLockVersion);
    }

    private QueryWrapper<GraphMaterialEventDO> byMaterialRefAndType(ContentRef ref, GraphMaterialEventType type) {
        QueryWrapper<GraphMaterialEventDO> w = new QueryWrapper<>();
        return w.eq("content_type", ContentRefCodec.toContentType(ref))
                .eq("content_ref_id", ContentRefCodec.toValue(ref))
                .eq("event_type", type.value());
    }

    private QueryWrapper<GraphMaterialEventDO> eventQuery(
            ContentRef ref, GraphMaterialEventType type, GraphMaterialEventStatus status) {
        QueryWrapper<GraphMaterialEventDO> w = new QueryWrapper<>();
        if (ref != null) {
            w.eq("content_type", ContentRefCodec.toContentType(ref)).eq("content_ref_id", ContentRefCodec.toValue(ref));
        }
        if (type != null) {
            w.eq("event_type", type.value());
        }
        if (status != null) {
            w.eq("status", status.value());
        }
        return w;
    }

    private int offset(int pageNo, int pageSize) {
        return (pageNo - 1) * pageSize;
    }
}
