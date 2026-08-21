package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphEdgeKeyCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeSlice;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedEdgeMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphPublishedEdgeRepositoryImpl implements GraphPublishedEdgeRepository {
    private static final int ONE_HOP_EDGE_BATCH_SIZE = 50;

    private final GraphPublishedEdgeMapper mapper;

    public GraphPublishedEdgeRepositoryImpl(GraphPublishedEdgeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphPublishedEdge getById(GraphPublishedEdgeId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphPublishedEdgeIdCodec.toValue(id)));
    }

    @Override
    public GraphPublishedEdge getByEdgeKey(GraphEdgeKey key) {
        QueryWrapper<GraphPublishedEdgeDO> w = new QueryWrapper<>();
        return GraphPersistenceAssembler.toDomain(mapper.selectOne(w.eq("edge_key", GraphEdgeKeyCodec.toValue(key))));
    }

    @Override
    public List<GraphPublishedEdge> listByNodeIds(List<GraphPublishedNodeId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Long> values = ids.stream().map(GraphPublishedNodeIdCodec::toValue).toList();
        QueryWrapper<GraphPublishedEdgeDO> w = new QueryWrapper<>();
        return mapper
                .selectList(w.nested(
                        q -> q.in("source_published_node_id", values).or().in("target_published_node_id", values)))
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<GraphPublishedEdge> listByIds(List<GraphPublishedEdgeId> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper
                .selectBatchIds(
                        ids.stream().map(GraphPublishedEdgeIdCodec::toValue).toList())
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<GraphPublishedEdge> listRecentlyUpdated(int limit) {
        int effectiveLimit = limit <= 0 ? 1 : limit;
        return mapper.listRecentlyUpdated(effectiveLimit).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<GraphPublishedEdge> listRecentlyUpdatedByMaterials(
            List<com.thundax.kuzhambu.common.core.content.valueobject.ContentRef> materialRefs, int limit) {
        if (materialRefs == null || materialRefs.isEmpty()) {
            return List.of();
        }
        int effectiveLimit = limit <= 0 ? 1 : limit;
        return mapper.listRecentlyUpdatedByMaterials(materialRefs, effectiveLimit).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public GraphPublishedEdgeSlice listOneHopEdges(List<GraphPublishedNodeId> ids, GraphPublishedEdgeId after) {
        if (ids == null || ids.isEmpty()) {
            return new GraphPublishedEdgeSlice(List.of(), null, false);
        }
        List<Long> values = ids.stream().map(GraphPublishedNodeIdCodec::toValue).toList();
        List<GraphPublishedEdge> edges =
                mapper
                        .listOneHopEdges(values, GraphPublishedEdgeIdCodec.toValue(after), ONE_HOP_EDGE_BATCH_SIZE + 1)
                        .stream()
                        .map(GraphPersistenceAssembler::toDomain)
                        .toList();
        boolean truncated = edges.size() > ONE_HOP_EDGE_BATCH_SIZE;
        List<GraphPublishedEdge> page = truncated ? edges.subList(0, ONE_HOP_EDGE_BATCH_SIZE) : edges;
        return new GraphPublishedEdgeSlice(
                page, truncated ? page.get(page.size() - 1).getId() : null, truncated);
    }

    @Override
    public PageResult<GraphPublishedEdge> page(
            String keyword,
            String relationType,
            GraphPublishedStatus status,
            GraphSourceType source,
            int pageNo,
            int pageSize) {
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;
        long total = mapper.selectCount(edgeQuery(keyword, relationType, status, source));
        QueryWrapper<GraphPublishedEdgeDO> pageWrapper = edgeQuery(keyword, relationType, status, source)
                .orderByDesc("modified_at")
                .orderByDesc("id")
                .last("limit " + offset(effectivePageNo, effectivePageSize) + ", " + effectivePageSize);
        List<GraphPublishedEdge> records = mapper.selectList(pageWrapper).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
        return PageResult.of(effectivePageNo, effectivePageSize, total, records);
    }

    @Override
    public GraphPublishedEdgeId insert(GraphPublishedEdge edge) {
        GraphPublishedEdgeDO dataObject = GraphPersistenceAssembler.toObject(edge);
        mapper.insert(dataObject);
        return GraphPublishedEdgeIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(GraphPublishedEdge edge) {
        return mapper.updateById(GraphPersistenceAssembler.toObject(edge));
    }

    @Override
    public int updateIfLockVersion(GraphPublishedEdge edge, long expectedLockVersion) {
        return mapper.updateIfLockVersion(GraphPersistenceAssembler.toObject(edge), expectedLockVersion);
    }

    @Override
    public long count(GraphPublishedStatus status) {
        QueryWrapper<GraphPublishedEdgeDO> w = new QueryWrapper<>();
        if (status != null) {
            w.eq("status", status.value());
        }
        return mapper.selectCount(w);
    }

    private QueryWrapper<GraphPublishedEdgeDO> edgeQuery(
            String keyword, String relationType, GraphPublishedStatus status, GraphSourceType source) {
        QueryWrapper<GraphPublishedEdgeDO> w = new QueryWrapper<>();
        if (!isBlank(keyword)) {
            w.nested(v -> v.like("edge_key", keyword).or().like("relation_type", keyword));
        }
        if (!isBlank(relationType)) {
            w.eq("relation_type", relationType);
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
