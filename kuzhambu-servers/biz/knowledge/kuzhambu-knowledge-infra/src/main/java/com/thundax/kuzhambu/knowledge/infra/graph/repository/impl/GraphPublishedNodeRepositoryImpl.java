package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphNodeKeyCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphPublishedAdjacency;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedNodeMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphPublishedNodeRepositoryImpl implements GraphPublishedNodeRepository {
    private final GraphPublishedNodeMapper mapper;

    public GraphPublishedNodeRepositoryImpl(GraphPublishedNodeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphPublishedNode getById(GraphPublishedNodeId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphPublishedNodeIdCodec.toValue(id)));
    }

    @Override
    public GraphPublishedNode getByNodeKey(GraphNodeKey key) {
        QueryWrapper<GraphPublishedNodeDO> w = new QueryWrapper<>();
        return GraphPersistenceAssembler.toDomain(mapper.selectOne(w.eq("node_key", GraphNodeKeyCodec.toValue(key))));
    }

    @Override
    public List<GraphPublishedNode> listByIds(List<GraphPublishedNodeId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return mapper
                .selectBatchIds(
                        ids.stream().map(GraphPublishedNodeIdCodec::toValue).toList())
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<GraphPublishedNode> listRecentlyUpdated(int limit) {
        QueryWrapper<GraphPublishedNodeDO> w = new QueryWrapper<>();
        int effectiveLimit = limit <= 0 ? 100 : limit;
        return mapper
                .selectList(w.eq("status", GraphPublishedStatus.ACTIVE.value())
                        .orderByDesc("modified_at")
                        .orderByDesc("id")
                        .last("limit " + effectiveLimit))
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public PageResult<GraphPublishedNode> page(
            String keyword,
            GraphNodeType nodeType,
            GraphPublishedStatus status,
            GraphSourceType source,
            int pageNo,
            int pageSize) {
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;
        long total = mapper.selectCount(nodeQuery(keyword, nodeType, status, source));
        QueryWrapper<GraphPublishedNodeDO> pageWrapper = nodeQuery(keyword, nodeType, status, source)
                .orderByDesc("modified_at")
                .orderByDesc("id")
                .last("limit " + offset(effectivePageNo, effectivePageSize) + ", " + effectivePageSize);
        List<GraphPublishedNode> records = mapper.selectList(pageWrapper).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
        return PageResult.of(effectivePageNo, effectivePageSize, total, records);
    }

    @Override
    public PageResult<GraphPublishedAdjacency> pageAdjacency(
            String subjectKeyword,
            GraphNodeType subjectType,
            GraphPublishedStatus subjectStatus,
            GraphSourceType subjectSource,
            String relationType,
            GraphPublishedStatus relationStatus,
            GraphSourceType relationSource,
            String objectKeyword,
            GraphNodeType objectType,
            GraphPublishedStatus objectStatus,
            GraphSourceType objectSource,
            boolean includeIsolated,
            int pageNo,
            int pageSize) {
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;
        String subjectTypeValue = subjectType == null ? null : subjectType.value();
        String subjectStatusValue = subjectStatus == null ? null : subjectStatus.value();
        String subjectSourceValue = subjectSource == null ? null : subjectSource.value();
        String relationStatusValue = relationStatus == null ? null : relationStatus.value();
        String relationSourceValue = relationSource == null ? null : relationSource.value();
        String objectTypeValue = objectType == null ? null : objectType.value();
        String objectStatusValue = objectStatus == null ? null : objectStatus.value();
        String objectSourceValue = objectSource == null ? null : objectSource.value();
        long total = mapper.countAdjacency(
                subjectKeyword,
                subjectTypeValue,
                subjectStatusValue,
                subjectSourceValue,
                relationType,
                relationStatusValue,
                relationSourceValue,
                objectKeyword,
                objectTypeValue,
                objectStatusValue,
                objectSourceValue,
                includeIsolated);
        List<GraphPublishedAdjacency> records = mapper
                .listAdjacency(
                        subjectKeyword,
                        subjectTypeValue,
                        subjectStatusValue,
                        subjectSourceValue,
                        relationType,
                        relationStatusValue,
                        relationSourceValue,
                        objectKeyword,
                        objectTypeValue,
                        objectStatusValue,
                        objectSourceValue,
                        includeIsolated,
                        offset(effectivePageNo, effectivePageSize),
                        effectivePageSize)
                .stream()
                .map(row -> new GraphPublishedAdjacency(
                        GraphPersistenceAssembler.toSubjectDomain(row),
                        GraphPersistenceAssembler.toRelationDomain(row),
                        GraphPersistenceAssembler.toObjectDomain(row)))
                .toList();
        return PageResult.of(effectivePageNo, effectivePageSize, total, records);
    }

    @Override
    public GraphPublishedNodeId insert(GraphPublishedNode node) {
        GraphPublishedNodeDO dataObject = GraphPersistenceAssembler.toObject(node);
        mapper.insert(dataObject);
        return GraphPublishedNodeIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(GraphPublishedNode node) {
        return mapper.updateById(GraphPersistenceAssembler.toObject(node));
    }

    @Override
    public int updateIfLockVersion(GraphPublishedNode node, long expectedLockVersion) {
        return mapper.updateIfLockVersion(GraphPersistenceAssembler.toObject(node), expectedLockVersion);
    }

    @Override
    public long count(GraphPublishedStatus status) {
        QueryWrapper<GraphPublishedNodeDO> w = new QueryWrapper<>();
        if (status != null) {
            w.eq("status", status.value());
        }
        return mapper.selectCount(w);
    }

    private QueryWrapper<GraphPublishedNodeDO> nodeQuery(
            String keyword, GraphNodeType nodeType, GraphPublishedStatus status, GraphSourceType source) {
        QueryWrapper<GraphPublishedNodeDO> w = new QueryWrapper<>();
        if (!isBlank(keyword)) {
            w.like("name", keyword);
        }
        if (nodeType != null) {
            w.eq("node_type", nodeType.value());
        }
        if (status != null) {
            w.eq("status", status.value());
        }
        if (source != null) {
            w.eq("source", source.value());
        }
        return w;
    }

    private int offset(int pageNo, int pageSize) {
        return (pageNo - 1) * pageSize;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
