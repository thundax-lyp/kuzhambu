package com.thundax.kuzhambu.knowledge.application.lineage.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.AvailableFiltersView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.EmptyView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.NodeView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.RelationView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.SourceRefView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.SummaryView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.VersionOptionView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.VersionView;
import com.thundax.kuzhambu.knowledge.application.lineage.service.KnowledgeLineageReadApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class KnowledgeLineageReadApplicationServiceImpl implements KnowledgeLineageReadApplicationService {

    private static final String TASK_TYPE_LINEAGE = "LINEAGE";
    private static final String STATUS_APPLIED = "APPLIED";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final int DEFAULT_DEPTH = 2;
    private static final int MAX_DEPTH = 4;
    private static final int VERSION_OPTION_LIMIT = 200;

    private final GraphVersionRepository graphVersionRepository;
    private final KnowledgeLineageNodeRepository lineageNodeRepository;
    private final KnowledgeLineageRelationRepository lineageRelationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public KnowledgeLineageReadApplicationServiceImpl(
            GraphVersionRepository graphVersionRepository,
            KnowledgeLineageNodeRepository lineageNodeRepository,
            KnowledgeLineageRelationRepository lineageRelationRepository) {
        this.graphVersionRepository = graphVersionRepository;
        this.lineageNodeRepository = lineageNodeRepository;
        this.lineageRelationRepository = lineageRelationRepository;
    }

    @Override
    public LineageCanvasResult getCanvas(LineageCanvasQuery query) {
        LineageCanvasQuery effectiveQuery = normalizeQuery(query);
        AvailableFiltersView filters = buildAvailableFilters();
        if (effectiveQuery.getVersionId() == null) {
            return emptyResult(filters, "NO_VERSION", "请选择世系版本", "选择一个已应用版本后浏览正式世系节点和关系。", null, null);
        }
        GraphVersion version = graphVersionRepository.getByVersionId(effectiveQuery.getVersionId());
        if (version == null || !TASK_TYPE_LINEAGE.equals(version.getTaskType())) {
            return emptyResult(filters, "NO_VERSION", "未找到世系版本", "当前版本不存在或不是世系图版本。", null, null);
        }
        return buildCanvas(version, effectiveQuery, filters);
    }

    @Override
    public LineageCanvasResult getLatestAppliedCanvas(LineageCanvasQuery query) {
        LineageCanvasQuery effectiveQuery = normalizeQuery(query);
        if (effectiveQuery.getVersionId() == null) {
            PageResult<GraphVersion> latestPage =
                    graphVersionRepository.page(TASK_TYPE_LINEAGE, STATUS_APPLIED, null, null, 1, 1);
            GraphVersion latest = latestPage.getRecords().isEmpty()
                    ? null
                    : latestPage.getRecords().get(0);
            effectiveQuery.setVersionId(latest == null ? null : latest.getId());
        }
        return getCanvas(effectiveQuery);
    }

    private LineageCanvasResult buildCanvas(
            GraphVersion version, LineageCanvasQuery query, AvailableFiltersView availableFilters) {
        List<KnowledgeLineageNode> allNodes = defaultList(lineageNodeRepository.listByVersionId(version.getId()));
        List<KnowledgeLineageRelation> allRelations =
                defaultList(lineageRelationRepository.listByVersionId(version.getId()));
        if (allNodes.isEmpty() && allRelations.isEmpty()) {
            return emptyResult(availableFilters, "NO_LINEAGE_DATA", "暂无世系数据", "当前版本尚未沉淀正式世系节点或关系。", null, null);
        }

        Map<String, KnowledgeLineageNode> nodesByKey = allNodes.stream()
                .filter(node -> StringUtils.isNotBlank(node.getNodeKey()))
                .collect(Collectors.toMap(
                        KnowledgeLineageNode::getNodeKey, node -> node, (left, right) -> left, LinkedHashMap::new));
        Set<String> focusedNodeKeys = resolveFocusedNodeKeys(allNodes, allRelations, query);
        List<KnowledgeLineageNode> scopedNodes = allNodes.stream()
                .filter(node -> focusedNodeKeys.isEmpty() || focusedNodeKeys.contains(node.getNodeKey()))
                .filter(node -> matchesNode(node, query))
                .toList();
        Set<String> visibleNodeKeys = scopedNodes.stream()
                .map(KnowledgeLineageNode::getNodeKey)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<KnowledgeLineageRelation> scopedRelations = allRelations.stream()
                .filter(relation -> focusedNodeKeys.isEmpty()
                        || focusedNodeKeys.contains(relation.getSourceNodeKey())
                        || focusedNodeKeys.contains(relation.getTargetNodeKey()))
                .filter(relation -> visibleNodeKeys.contains(relation.getSourceNodeKey())
                        && visibleNodeKeys.contains(relation.getTargetNodeKey()))
                .filter(relation -> matchesRelation(relation, query))
                .toList();

        if (scopedNodes.isEmpty() && scopedRelations.isEmpty()) {
            return emptyResult(availableFilters, "FILTER_NO_RESULT", "没有匹配结果", "调整关键词、类型或确认状态后再试。", "重置筛选", null);
        }

        Map<String, KnowledgeLineageNode> visibleNodesByKey = scopedNodes.stream()
                .filter(node -> StringUtils.isNotBlank(node.getNodeKey()))
                .collect(Collectors.toMap(
                        KnowledgeLineageNode::getNodeKey, node -> node, (left, right) -> left, LinkedHashMap::new));
        List<NodeView> nodeViews = scopedNodes.stream().map(this::toNodeView).toList();
        List<RelationView> relationViews = scopedRelations.stream()
                .map(relation -> toRelationView(relation, visibleNodesByKey, nodesByKey))
                .toList();
        NodeView selectedNode = nodeViews.stream()
                .filter(node -> Objects.equals(node.getNodeId(), query.getFocusNodeId()))
                .findFirst()
                .orElse(null);
        RelationView selectedRelation = relationViews.stream()
                .filter(relation -> Objects.equals(relation.getRelationId(), query.getFocusRelationId()))
                .findFirst()
                .orElse(null);
        SummaryView summary = new SummaryView(
                (long) nodeViews.size(),
                (long) relationViews.size(),
                nodeViews.stream()
                        .filter(node -> STATUS_CONFIRMED.equals(node.getConfirmationStatus()))
                        .count(),
                relationViews.stream()
                        .filter(relation -> STATUS_CONFIRMED.equals(relation.getConfirmationStatus()))
                        .count(),
                query.getFocusNodeId(),
                query.getFocusRelationId());
        return new LineageCanvasResult(
                toVersionView(version),
                summary,
                nodeViews,
                relationViews,
                selectedNode,
                selectedRelation,
                availableFilters,
                null);
    }

    private Set<String> resolveFocusedNodeKeys(
            List<KnowledgeLineageNode> nodes, List<KnowledgeLineageRelation> relations, LineageCanvasQuery query) {
        String focusNodeKey = nodes.stream()
                .filter(node -> Objects.equals(node.getNodeId(), query.getFocusNodeId()))
                .map(KnowledgeLineageNode::getNodeKey)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
        if (focusNodeKey == null && query.getFocusRelationId() != null) {
            KnowledgeLineageRelation relation = relations.stream()
                    .filter(item -> Objects.equals(item.getRelationId(), query.getFocusRelationId()))
                    .findFirst()
                    .orElse(null);
            focusNodeKey = relation == null ? null : relation.getSourceNodeKey();
        }
        if (StringUtils.isBlank(focusNodeKey)) {
            return Set.of();
        }
        Map<String, Set<String>> adjacency = new LinkedHashMap<>();
        for (KnowledgeLineageRelation relation : relations) {
            if (StringUtils.isBlank(relation.getSourceNodeKey()) || StringUtils.isBlank(relation.getTargetNodeKey())) {
                continue;
            }
            adjacency
                    .computeIfAbsent(relation.getSourceNodeKey(), ignored -> new LinkedHashSet<>())
                    .add(relation.getTargetNodeKey());
            adjacency
                    .computeIfAbsent(relation.getTargetNodeKey(), ignored -> new LinkedHashSet<>())
                    .add(relation.getSourceNodeKey());
        }
        int maxDepth = normalizeDepth(query.getDepth());
        Set<String> visited = new LinkedHashSet<>();
        ArrayDeque<NodeDepth> queue = new ArrayDeque<>();
        queue.add(new NodeDepth(focusNodeKey, 0));
        visited.add(focusNodeKey);
        while (!queue.isEmpty()) {
            NodeDepth current = queue.removeFirst();
            if (current.depth() >= maxDepth) {
                continue;
            }
            for (String next : adjacency.getOrDefault(current.nodeKey(), Set.of())) {
                if (visited.add(next)) {
                    queue.addLast(new NodeDepth(next, current.depth() + 1));
                }
            }
        }
        return visited;
    }

    private AvailableFiltersView buildAvailableFilters() {
        PageResult<GraphVersion> versionPage =
                graphVersionRepository.page(TASK_TYPE_LINEAGE, STATUS_APPLIED, null, null, 1, VERSION_OPTION_LIMIT);
        List<VersionOptionView> versions =
                versionPage.getRecords().stream().map(this::toVersionOptionView).toList();
        List<String> nodeTypes = new ArrayList<>();
        List<String> relationTypes = new ArrayList<>();
        List<String> confirmationStatuses = new ArrayList<>();
        for (GraphVersion version : versionPage.getRecords()) {
            Long versionId = version.getId();
            for (KnowledgeLineageNode node : defaultList(lineageNodeRepository.listByVersionId(versionId))) {
                addDistinct(nodeTypes, node.getNodeType());
                addDistinct(confirmationStatuses, node.getConfirmationStatus());
            }
            for (KnowledgeLineageRelation relation :
                    defaultList(lineageRelationRepository.listByVersionId(versionId))) {
                addDistinct(relationTypes, relation.getRelationType());
                addDistinct(confirmationStatuses, relation.getConfirmationStatus());
            }
        }
        nodeTypes.sort(Comparator.naturalOrder());
        relationTypes.sort(Comparator.naturalOrder());
        confirmationStatuses.sort(Comparator.naturalOrder());
        return new AvailableFiltersView(versions, nodeTypes, relationTypes, confirmationStatuses);
    }

    private boolean matchesNode(KnowledgeLineageNode node, LineageCanvasQuery query) {
        return matchesKeyword(node, query.getKeyword())
                && matchesValue(node.getNodeType(), query.getNodeType())
                && matchesValue(node.getConfirmationStatus(), query.getConfirmationStatus());
    }

    private boolean matchesRelation(KnowledgeLineageRelation relation, LineageCanvasQuery query) {
        return matchesKeyword(relation, query.getKeyword())
                && matchesValue(relation.getRelationType(), query.getRelationType())
                && matchesValue(relation.getConfirmationStatus(), query.getConfirmationStatus());
    }

    private boolean matchesKeyword(KnowledgeLineageNode node, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return true;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        return contains(node.getName(), normalized)
                || contains(node.getNodeKey(), normalized)
                || contains(node.getNodeType(), normalized);
    }

    private boolean matchesKeyword(KnowledgeLineageRelation relation, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return true;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        return contains(relation.getSourceName(), normalized)
                || contains(relation.getTargetName(), normalized)
                || contains(relation.getRelationType(), normalized)
                || contains(relation.getEvidence(), normalized);
    }

    private NodeView toNodeView(KnowledgeLineageNode node) {
        return new NodeView(
                "lineage-node:" + node.getNodeId(),
                node.getNodeId(),
                node.getNodeKey(),
                node.getName(),
                node.getNodeType(),
                node.getGeneration(),
                node.getGender(),
                node.getConfirmationStatus(),
                null,
                node.getSourceRefsJson(),
                parseSourceRefs(node.getSourceRefsJson()),
                toEpochMillis(node.getFirstExtractedAt()),
                toEpochMillis(node.getLastExtractedAt()),
                null,
                null);
    }

    private RelationView toRelationView(
            KnowledgeLineageRelation relation,
            Map<String, KnowledgeLineageNode> visibleNodesByKey,
            Map<String, KnowledgeLineageNode> nodesByKey) {
        KnowledgeLineageNode sourceNode = firstNonNull(
                visibleNodesByKey.get(relation.getSourceNodeKey()), nodesByKey.get(relation.getSourceNodeKey()));
        KnowledgeLineageNode targetNode = firstNonNull(
                visibleNodesByKey.get(relation.getTargetNodeKey()), nodesByKey.get(relation.getTargetNodeKey()));
        return new RelationView(
                "lineage-relation:" + relation.getRelationId(),
                relation.getRelationId(),
                sourceNode == null ? null : sourceNode.getNodeId(),
                sourceNode == null ? relation.getSourceName() : sourceNode.getName(),
                targetNode == null ? null : targetNode.getNodeId(),
                targetNode == null ? relation.getTargetName() : targetNode.getName(),
                relation.getRelationType(),
                relationLabel(relation.getRelationType()),
                relation.getConfirmationStatus(),
                null,
                relation.getSourceRefsJson(),
                parseSourceRefs(relation.getSourceRefsJson()),
                toEpochMillis(relation.getFirstExtractedAt()),
                toEpochMillis(relation.getLastExtractedAt()));
    }

    private List<SourceRefView> parseSourceRefs(String sourceRefsJson) {
        if (StringUtils.isBlank(sourceRefsJson)) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(sourceRefsJson);
            if (root == null || root.isNull()) {
                return List.of();
            }
            if (root.isArray()) {
                List<SourceRefView> refs = new ArrayList<>();
                for (JsonNode item : root) {
                    refs.add(toSourceRefView(item));
                }
                return refs;
            }
            return List.of(toSourceRefView(root));
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private SourceRefView toSourceRefView(JsonNode node) {
        return new SourceRefView(
                text(node, "sourceContentType", "source_content_type", "contentType", "content_type"),
                longValue(node, "sourceContentId", "source_content_id", "contentId", "content_id"),
                text(node, "sourceTitle", "source_title", "title"),
                text(node, "snippet", "evidence", "quote"),
                text(node, "href", "url", "link"));
    }

    private LineageCanvasQuery normalizeQuery(LineageCanvasQuery query) {
        LineageCanvasQuery effective = query == null ? new LineageCanvasQuery() : query;
        effective.setKeyword(StringUtils.trimToNull(effective.getKeyword()));
        effective.setNodeType(StringUtils.trimToNull(effective.getNodeType()));
        effective.setRelationType(StringUtils.trimToNull(effective.getRelationType()));
        effective.setConfirmationStatus(StringUtils.trimToNull(effective.getConfirmationStatus()));
        effective.setDepth(normalizeDepth(effective.getDepth()));
        return effective;
    }

    private int normalizeDepth(Integer depth) {
        if (depth == null || depth < 1) {
            return DEFAULT_DEPTH;
        }
        return Math.min(depth, MAX_DEPTH);
    }

    private LineageCanvasResult emptyResult(
            AvailableFiltersView filters,
            String reason,
            String title,
            String description,
            String actionLabel,
            String actionHref) {
        return new LineageCanvasResult(
                null,
                new SummaryView(0L, 0L, 0L, 0L, null, null),
                List.of(),
                List.of(),
                null,
                null,
                filters,
                new EmptyView(reason, title, description, actionLabel, actionHref));
    }

    private VersionView toVersionView(GraphVersion version) {
        return new VersionView(
                version.getId(),
                version.getVersionNo(),
                version.getTaskType(),
                version.getStatus(),
                version.getSourceContentType(),
                version.getSourceContentId(),
                version.getSourceCategoryCode(),
                version.getSourceCategoryName(),
                toEpochMillis(version.getAppliedAt()));
    }

    private VersionOptionView toVersionOptionView(GraphVersion version) {
        return new VersionOptionView(
                version.getId(),
                version.getVersionNo(),
                version.getTaskType(),
                version.getStatus(),
                version.getSourceContentType(),
                version.getSourceContentId(),
                version.getSourceCategoryCode(),
                version.getSourceCategoryName(),
                toEpochMillis(version.getAppliedAt()));
    }

    private String relationLabel(String relationType) {
        return StringUtils.defaultIfBlank(relationType, "关系");
    }

    private boolean matchesValue(String actual, String expected) {
        return StringUtils.isBlank(expected) || StringUtils.equals(actual, expected);
    }

    private boolean contains(String actual, String normalizedKeyword) {
        return StringUtils.isNotBlank(actual) && actual.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    private void addDistinct(List<String> values, String value) {
        if (StringUtils.isNotBlank(value) && !values.contains(value)) {
            values.add(value);
        }
    }

    private Long toEpochMillis(Date date) {
        return date == null ? null : date.getTime();
    }

    private String text(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && !value.isNull() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return null;
    }

    private Long longValue(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value == null || value.isNull()) {
                continue;
            }
            if (value.isNumber()) {
                return value.asLong();
            }
            if (StringUtils.isNumeric(value.asText())) {
                return Long.valueOf(value.asText());
            }
        }
        return null;
    }

    private <T> List<T> defaultList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private <T> T firstNonNull(T left, T right) {
        return left == null ? right : left;
    }

    private record NodeDepth(String nodeKey, int depth) {}
}
