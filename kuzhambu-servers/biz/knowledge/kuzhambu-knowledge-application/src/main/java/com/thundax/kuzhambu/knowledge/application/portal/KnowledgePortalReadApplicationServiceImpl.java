package com.thundax.kuzhambu.knowledge.application.portal;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.IssueRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.ReportRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.SourceDetailRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeQualityReportApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagGovernanceMetricsRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class KnowledgePortalReadApplicationServiceImpl implements KnowledgePortalReadApplicationService {

    private static final int FIRST_PAGE_NO = 1;
    private static final int COUNT_PAGE_SIZE = 1;
    private static final int RECENT_UPDATE_LIMIT = 3;
    private static final int DEFAULT_METRICS_TOP_LIMIT = 5;
    private static final int DEFAULT_METRICS_MONTHS = 6;
    private static final String GRAPH_VERSION_APPLIED_STATUS = "APPLIED";
    private static final String REFINEMENT_DRAFT_STATUS = "DRAFT";
    private static final List<CategorySlot> SANCAI_CATEGORY_SLOTS = List.of(
            new CategorySlot("ASTRONOMY", "天文"),
            new CategorySlot("GEOGRAPHY", "地理"),
            new CategorySlot("PEOPLE", "人物"),
            new CategorySlot("SEASONS", "时令"),
            new CategorySlot("PALACES", "宫室"),
            new CategorySlot("TOOLS", "器用"),
            new CategorySlot("BODY", "身体"),
            new CategorySlot("CLOTHING", "衣服"),
            new CategorySlot("AFFAIRS", "人事"),
            new CategorySlot("RITUALS", "仪制"),
            new CategorySlot("TREASURES", "珍宝"),
            new CategorySlot("LITERATURE", "文史"),
            new CategorySlot("ANIMALS", "鸟兽"),
            new CategorySlot("PLANTS", "草木"));

    private final TagRepository tagRepository;
    private final GraphVersionRepository graphVersionRepository;
    private final KnowledgeEntityRepository knowledgeEntityRepository;
    private final KnowledgeRelationRepository knowledgeRelationRepository;
    private final TagGovernanceMetricsRepository tagGovernanceMetricsRepository;
    private final RefinementTaskRepository refinementTaskRepository;
    private final KnowledgeQualityReportApplicationService qualityReportApplicationService;

    public KnowledgePortalReadApplicationServiceImpl(
            TagRepository tagRepository,
            GraphVersionRepository graphVersionRepository,
            KnowledgeEntityRepository knowledgeEntityRepository,
            KnowledgeRelationRepository knowledgeRelationRepository,
            TagGovernanceMetricsRepository tagGovernanceMetricsRepository,
            RefinementTaskRepository refinementTaskRepository,
            KnowledgeQualityReportApplicationService qualityReportApplicationService) {
        this.tagRepository = tagRepository;
        this.graphVersionRepository = graphVersionRepository;
        this.knowledgeEntityRepository = knowledgeEntityRepository;
        this.knowledgeRelationRepository = knowledgeRelationRepository;
        this.tagGovernanceMetricsRepository = tagGovernanceMetricsRepository;
        this.refinementTaskRepository = refinementTaskRepository;
        this.qualityReportApplicationService = qualityReportApplicationService;
    }

    @Override
    public KnowledgePortalHomeResult getHome() {
        long tagCount = tagRepository
                .page(null, null, null, null, null, FIRST_PAGE_NO, COUNT_PAGE_SIZE)
                .getTotalCount();
        long graphVersionCount = graphVersionRepository
                .page(null, GRAPH_VERSION_APPLIED_STATUS, null, null, FIRST_PAGE_NO, COUNT_PAGE_SIZE)
                .getTotalCount();
        long entityCount = knowledgeEntityRepository
                .page(null, null, null, null, FIRST_PAGE_NO, COUNT_PAGE_SIZE)
                .getTotalCount();
        long relationCount = knowledgeRelationRepository
                .page(null, null, null, null, FIRST_PAGE_NO, COUNT_PAGE_SIZE)
                .getTotalCount();
        return new KnowledgePortalHomeResult(
                "古籍知识图谱门户",
                "以图谱、关系与来源线索浏览古籍知识沉淀，聚焦阅读、追溯与质量理解。",
                "搜索人物、器物、礼制、典故或标签",
                List.of(
                        stat("tag-count", "主题标签", tagCount, "统一知识命名基线", "steady", "seal"),
                        stat("graph-version-count", "图谱版本", graphVersionCount, "已完成应用的知识快照", "up", "scroll"),
                        stat("entity-count", "知识实体", entityCount, "可直接进入关系浏览", "up", "jade"),
                        stat("relation-count", "关联关系", relationCount, "支撑来源与脉络追溯", "up", "constellation")),
                List.of(
                        quickLink("atlas", "图谱浏览", "进入知识关系画布与实体详情", "/knowledge/atlas", "atlas"),
                        quickLink("quality", "质量总览", "查看覆盖率、置信度与待关注项", "/knowledge/quality", "quality"),
                        quickLink("lineage", "来源追溯", "从版本、实体与关系回看来源脉络", "/knowledge/atlas", "lineage")),
                buildRecentUpdates(),
                List.of(
                        featureCollection("latest-atlas", "最新图谱版本", "快速进入最近一次已应用的知识快照。", "/knowledge/atlas", "版本视图"),
                        featureCollection("entity-gallery", "实体总览", "从人物、器物、礼制等实体切入浏览关联。", "/knowledge/atlas", "关系阅读"),
                        featureCollection("quality-brief", "质量摘要", "用阅读型摘要理解当前知识资产状态。", "/knowledge/quality", "质量洞察")));
    }

    @Override
    public KnowledgePortalAtlasResult getAtlas(KnowledgePortalAtlasQuery query) {
        KnowledgePortalAtlasQuery effectiveQuery = normalizeAtlasQuery(query);
        if ("overview".equalsIgnoreCase(effectiveQuery.getLevel())) {
            return buildOverviewAtlas();
        }
        if ("category".equalsIgnoreCase(effectiveQuery.getLevel())) {
            return buildCategoryAtlas(effectiveQuery.getCategoryCode());
        }
        if ("detail".equalsIgnoreCase(effectiveQuery.getLevel())) {
            return buildDetailAtlas(effectiveQuery.getEntityId());
        }
        return buildOverviewAtlas();
    }

    private KnowledgePortalAtlasResult buildOverviewAtlas() {
        List<GraphVersion> appliedVersions = defaultList(graphVersionRepository.listAppliedByCategoryCode(null));
        LinkedHashMap<String, List<GraphVersion>> versionsByCategory = appliedVersions.stream()
                .filter(version -> version.getSourceCategoryCode() != null
                        && !version.getSourceCategoryCode().isBlank())
                .collect(Collectors.groupingBy(
                        GraphVersion::getSourceCategoryCode, LinkedHashMap::new, Collectors.toList()));
        List<KnowledgePortalAtlasResult.OverviewCategoryCard> categoryCards = SANCAI_CATEGORY_SLOTS.stream()
                .map(slot -> toOverviewCategoryCard(slot, versionsByCategory.get(slot.code())))
                .toList();
        return new KnowledgePortalAtlasResult(
                "overview",
                List.of(new KnowledgePortalAtlasResult.BreadcrumbItem(
                        "overview", "图谱总览", "/knowledge/atlas?level=overview")),
                new KnowledgePortalAtlasResult.OverviewView("十四门类知识鸟瞰", "先看门类分布，再进入单门类浏览与单实体详情。", categoryCards),
                null,
                null,
                new KnowledgePortalAtlasResult.AvailableFilters(
                        distinctValues(appliedVersions.stream()
                                .map(GraphVersion::getSourceContentType)
                                .toList()),
                        List.of(),
                        List.of(),
                        List.of(),
                        defaultTimeRanges()),
                buildOverviewCanvas(categoryCards));
    }

    private KnowledgePortalAtlasResult.OverviewCategoryCard toOverviewCategoryCard(
            CategorySlot categorySlot, List<GraphVersion> versions) {
        if (versions == null || versions.isEmpty()) {
            return new KnowledgePortalAtlasResult.OverviewCategoryCard(
                    categorySlot.code(),
                    categorySlot.name(),
                    0L,
                    0L,
                    0L,
                    null,
                    "/knowledge/atlas?level=category&categoryCode=" + categorySlot.code());
        }
        GraphVersion latestVersion = versions.get(0);
        Long versionId = latestVersion.getVersionId();
        List<KnowledgeEntity> entities =
                versionId == null ? List.of() : defaultList(knowledgeEntityRepository.listByVersionId(versionId));
        List<KnowledgeRelation> relations =
                versionId == null ? List.of() : defaultList(knowledgeRelationRepository.listByVersionId(versionId));
        return new KnowledgePortalAtlasResult.OverviewCategoryCard(
                categorySlot.code(),
                categorySlot.name(),
                (long) entities.size(),
                (long) relations.size(),
                (long) versions.size(),
                latestVersion.getVersionNo(),
                "/knowledge/atlas?level=category&categoryCode=" + categorySlot.code());
    }

    private KnowledgePortalAtlasResult buildCategoryAtlas(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return buildOverviewAtlas();
        }
        CategorySlot categorySlot = findCategorySlot(categoryCode);
        if (categorySlot == null) {
            return buildOverviewAtlas();
        }
        GraphVersion latestVersion = graphVersionRepository.findLatestAppliedByCategoryCode(categoryCode);
        if (latestVersion == null || latestVersion.getVersionId() == null) {
            return buildEmptyCategoryAtlas(categorySlot);
        }
        Long versionId = latestVersion.getVersionId();
        List<KnowledgeEntity> entities = defaultList(knowledgeEntityRepository.listByVersionId(versionId));
        List<KnowledgeRelation> relations = defaultList(knowledgeRelationRepository.listByVersionId(versionId));
        return new KnowledgePortalAtlasResult(
                "category",
                List.of(
                        new KnowledgePortalAtlasResult.BreadcrumbItem(
                                "overview", "图谱总览", "/knowledge/atlas?level=overview"),
                        new KnowledgePortalAtlasResult.BreadcrumbItem(
                                "category",
                                categorySlot.name(),
                                "/knowledge/atlas?level=category&categoryCode=" + categoryCode)),
                null,
                new KnowledgePortalAtlasResult.CategoryView(
                        categorySlot.code(),
                        categorySlot.name(),
                        latestVersion.getVersionId(),
                        latestVersion.getVersionNo(),
                        entities.stream().map(this::toCategoryEntityHighlight).toList(),
                        buildCategoryRelationGroups(relations),
                        buildSourceReferences(latestVersion)),
                null,
                new KnowledgePortalAtlasResult.AvailableFilters(
                        distinctValues(List.of(latestVersion.getSourceContentType())),
                        distinctValues(entities.stream()
                                .map(KnowledgeEntity::getEntityType)
                                .toList()),
                        distinctValues(relations.stream()
                                .map(KnowledgeRelation::getRelationType)
                                .toList()),
                        List.of(),
                        defaultTimeRanges()),
                buildCategoryCanvas(categorySlot, latestVersion, entities, relations));
    }

    private KnowledgePortalAtlasResult buildDetailAtlas(Long entityId) {
        if (entityId == null) {
            return buildOverviewAtlas();
        }
        KnowledgeEntity focusEntity = knowledgeEntityRepository.getByEntityId(entityId);
        if (focusEntity == null) {
            return buildOverviewAtlas();
        }
        List<KnowledgeRelation> relations =
                defaultList(knowledgeRelationRepository.listByEntityKey(focusEntity.getEntityKey()));
        GraphVersion latestVersion = latestAppliedVersion();
        String categoryCode = latestVersion == null ? null : latestVersion.getSourceCategoryCode();
        String categoryName = latestVersion == null || latestVersion.getSourceCategoryName() == null
                ? "门类详情"
                : latestVersion.getSourceCategoryName();
        return new KnowledgePortalAtlasResult(
                "detail",
                List.of(
                        new KnowledgePortalAtlasResult.BreadcrumbItem(
                                "overview", "图谱总览", "/knowledge/atlas?level=overview"),
                        new KnowledgePortalAtlasResult.BreadcrumbItem(
                                "category",
                                categoryName,
                                categoryCode == null || categoryCode.isBlank()
                                        ? "/knowledge/atlas?level=overview"
                                        : "/knowledge/atlas?level=category&categoryCode=" + categoryCode),
                        new KnowledgePortalAtlasResult.BreadcrumbItem(
                                "detail", focusEntity.getName(), "/knowledge/atlas?level=detail&entityId=" + entityId)),
                null,
                null,
                new KnowledgePortalAtlasResult.DetailView(
                        toFocusNode(focusEntity),
                        buildRelationGroups(focusEntity, relations),
                        buildSourceReferences(latestVersion),
                        buildTimelineItems(focusEntity),
                        List.of()),
                new KnowledgePortalAtlasResult.AvailableFilters(
                        distinctValues(List.of(latestVersion == null ? null : latestVersion.getSourceContentType())),
                        distinctValues(List.of(focusEntity.getEntityType())),
                        distinctValues(relations.stream()
                                .map(KnowledgeRelation::getRelationType)
                                .toList()),
                        List.of(),
                        defaultTimeRanges()),
                buildDetailCanvas(focusEntity, relations));
    }

    private KnowledgePortalAtlasResult buildEmptyCategoryAtlas(CategorySlot categorySlot) {
        return new KnowledgePortalAtlasResult(
                "category",
                List.of(
                        new KnowledgePortalAtlasResult.BreadcrumbItem(
                                "overview", "图谱总览", "/knowledge/atlas?level=overview"),
                        new KnowledgePortalAtlasResult.BreadcrumbItem(
                                "category",
                                categorySlot.name(),
                                "/knowledge/atlas?level=category&categoryCode=" + categorySlot.code())),
                null,
                new KnowledgePortalAtlasResult.CategoryView(
                        categorySlot.code(), categorySlot.name(), null, null, List.of(), List.of(), List.of()),
                null,
                new KnowledgePortalAtlasResult.AvailableFilters(
                        List.of(), List.of(), List.of(), List.of(), defaultTimeRanges()),
                new KnowledgePortalAtlasResult.CanvasView(
                        "category",
                        categorySlot.name() + "知识图谱",
                        "该门类保留固定空位，等待图谱版本应用后展示实体关系。",
                        "category:" + categorySlot.code(),
                        true,
                        "暂无" + categorySlot.name() + "图谱",
                        "当前门类尚无已应用图谱版本，Portal 仅展示固定 14 门类空位。",
                        List.of(new KnowledgePortalAtlasResult.CanvasNode(
                                "category:" + categorySlot.code(),
                                "category",
                                categorySlot.name(),
                                "固定十四门类空位",
                                "实体",
                                0L,
                                "empty",
                                categorySlot.code(),
                                null,
                                "/knowledge/atlas?level=category&categoryCode=" + categorySlot.code(),
                                0D,
                                0D,
                                0D)),
                        List.of()));
    }

    private KnowledgePortalAtlasResult.CanvasView buildOverviewCanvas(
            List<KnowledgePortalAtlasResult.OverviewCategoryCard> categoryCards) {
        List<KnowledgePortalAtlasResult.CanvasNode> nodes = new java.util.ArrayList<>();
        List<KnowledgePortalAtlasResult.CanvasEdge> edges = new java.util.ArrayList<>();
        nodes.add(new KnowledgePortalAtlasResult.CanvasNode(
                "root:sancai",
                "root",
                "三才图会",
                "固定十四门类",
                "门类",
                (long) SANCAI_CATEGORY_SLOTS.size(),
                "active",
                null,
                null,
                "/knowledge/atlas?level=overview",
                1D,
                0D,
                0D));
        int size = categoryCards == null ? 0 : categoryCards.size();
        for (int index = 0; index < size; index++) {
            KnowledgePortalAtlasResult.OverviewCategoryCard card = categoryCards.get(index);
            double angle = (Math.PI * 2D * index) / size - Math.PI / 2D;
            double radius = 280D;
            String nodeId = "category:" + card.getCategoryCode();
            boolean empty = card.getAppliedVersionCount() == null || card.getAppliedVersionCount() == 0L;
            nodes.add(new KnowledgePortalAtlasResult.CanvasNode(
                    nodeId,
                    "category",
                    card.getCategoryName(),
                    empty ? "等待图谱版本" : "版本 " + card.getLatestVersionNo(),
                    "实体",
                    card.getEntityCount(),
                    empty ? "empty" : "active",
                    card.getCategoryCode(),
                    null,
                    card.getEntryHref(),
                    card.getEntityCount() == null ? 0D : card.getEntityCount().doubleValue(),
                    Math.cos(angle) * radius,
                    Math.sin(angle) * radius));
            edges.add(new KnowledgePortalAtlasResult.CanvasEdge(
                    "root:sancai->" + nodeId,
                    "root:sancai",
                    nodeId,
                    empty ? "空位" : "门类",
                    "CATEGORY",
                    empty ? 0.2D : 1D,
                    empty));
        }
        return new KnowledgePortalAtlasResult.CanvasView(
                "overview", "十四门类知识图谱", "固定展示三才图会十四个正式门类，空门类保留可进入空态。", "root:sancai", false, null, null, nodes, edges);
    }

    private KnowledgePortalAtlasResult.CanvasView buildCategoryCanvas(
            CategorySlot categorySlot,
            GraphVersion latestVersion,
            List<KnowledgeEntity> entities,
            List<KnowledgeRelation> relations) {
        Map<String, KnowledgePortalAtlasResult.CanvasNode> nodes = new LinkedHashMap<>();
        Map<String, String> nodeIdByEntityKey = new LinkedHashMap<>();
        String categoryNodeId = "category:" + categorySlot.code();
        nodes.put(
                categoryNodeId,
                new KnowledgePortalAtlasResult.CanvasNode(
                        categoryNodeId,
                        "category",
                        categorySlot.name(),
                        latestVersion.getVersionNo() == null ? "已应用版本" : "版本 " + latestVersion.getVersionNo(),
                        "实体",
                        (long) entities.size(),
                        entities.isEmpty() ? "empty" : "active",
                        categorySlot.code(),
                        null,
                        "/knowledge/atlas?level=category&categoryCode=" + categorySlot.code(),
                        (double) entities.size(),
                        0D,
                        0D));
        for (KnowledgeEntity entity : entities) {
            KnowledgePortalAtlasResult.CanvasNode node = toEntityCanvasNode(entity);
            nodes.put(node.getId(), node);
            if (entity.getEntityKey() != null && !entity.getEntityKey().isBlank()) {
                nodeIdByEntityKey.put(entity.getEntityKey(), node.getId());
            }
        }
        List<KnowledgePortalAtlasResult.CanvasEdge> edges = new java.util.ArrayList<>();
        for (KnowledgeEntity entity : entities) {
            edges.add(new KnowledgePortalAtlasResult.CanvasEdge(
                    categoryNodeId + "->entity:" + entity.getEntityId(),
                    categoryNodeId,
                    "entity:" + entity.getEntityId(),
                    "包含",
                    "CATEGORY_ENTITY",
                    confidenceOf(entity.getConfirmationStatus()),
                    false));
        }
        for (KnowledgeRelation relation : relations) {
            String sourceId = ensureRelationNode(
                    nodes, nodeIdByEntityKey, relation.getSourceEntityKey(), relation.getSourceName());
            String targetId = ensureRelationNode(
                    nodes, nodeIdByEntityKey, relation.getTargetEntityKey(), relation.getTargetName());
            edges.add(toCanvasEdge(relation, sourceId, targetId));
        }
        return new KnowledgePortalAtlasResult.CanvasView(
                "category",
                categorySlot.name() + "知识图谱",
                "展示当前门类最新已应用图谱版本中的实体与关系。",
                categoryNodeId,
                entities.isEmpty(),
                entities.isEmpty() ? "暂无" + categorySlot.name() + "实体" : null,
                entities.isEmpty() ? "当前版本尚未沉淀可展示实体，门类空位保持可见。" : null,
                List.copyOf(nodes.values()),
                edges);
    }

    private KnowledgePortalAtlasResult.CanvasView buildDetailCanvas(
            KnowledgeEntity focusEntity, List<KnowledgeRelation> relations) {
        Map<String, KnowledgePortalAtlasResult.CanvasNode> nodes = new LinkedHashMap<>();
        KnowledgePortalAtlasResult.CanvasNode focusNode = toEntityCanvasNode(focusEntity);
        nodes.put(focusNode.getId(), focusNode);
        List<KnowledgePortalAtlasResult.CanvasEdge> edges = new java.util.ArrayList<>();
        for (KnowledgeRelation relation : relations) {
            String sourceId = Objects.equals(focusEntity.getEntityKey(), relation.getSourceEntityKey())
                    ? focusNode.getId()
                    : ensureRelationNode(nodes, Map.of(), relation.getSourceEntityKey(), relation.getSourceName());
            String targetId = Objects.equals(focusEntity.getEntityKey(), relation.getTargetEntityKey())
                    ? focusNode.getId()
                    : ensureRelationNode(nodes, Map.of(), relation.getTargetEntityKey(), relation.getTargetName());
            edges.add(toCanvasEdge(relation, sourceId, targetId));
        }
        return new KnowledgePortalAtlasResult.CanvasView(
                "detail",
                focusEntity.getName() + "关系图谱",
                "展示焦点实体与直接相邻关系。",
                focusNode.getId(),
                edges.isEmpty(),
                edges.isEmpty() ? "暂无相邻关系" : null,
                edges.isEmpty() ? "该实体当前没有可展示的直接关系。" : null,
                List.copyOf(nodes.values()),
                edges);
    }

    private KnowledgePortalAtlasResult.CanvasNode toEntityCanvasNode(KnowledgeEntity entity) {
        return new KnowledgePortalAtlasResult.CanvasNode(
                "entity:" + entity.getEntityId(),
                "entity",
                entity.getName(),
                entity.getEntityType(),
                "置信",
                Math.round(confidenceOf(entity.getConfirmationStatus()) * 100D),
                entity.getConfirmationStatus(),
                null,
                entity.getEntityId(),
                "/knowledge/atlas?level=detail&entityId=" + entity.getEntityId(),
                confidenceOf(entity.getConfirmationStatus()),
                null,
                null);
    }

    private String ensureRelationNode(
            Map<String, KnowledgePortalAtlasResult.CanvasNode> nodes,
            Map<String, String> nodeIdByEntityKey,
            String entityKey,
            String label) {
        String safeEntityKey = safeKey(entityKey);
        String existingNodeId = nodeIdByEntityKey.get(safeEntityKey);
        if (existingNodeId != null) {
            return existingNodeId;
        }
        String nodeId = "entity-key:" + safeEntityKey;
        if (!nodes.containsKey(nodeId)) {
            nodes.put(
                    nodeId,
                    new KnowledgePortalAtlasResult.CanvasNode(
                            nodeId,
                            "entity",
                            label == null || label.isBlank() ? safeEntityKey : label,
                            "关系端点",
                            "置信",
                            null,
                            "RELATED",
                            null,
                            null,
                            null,
                            0.7D,
                            null,
                            null));
        }
        return nodeId;
    }

    private KnowledgePortalAtlasResult.CanvasEdge toCanvasEdge(
            KnowledgeRelation relation, String sourceId, String targetId) {
        return new KnowledgePortalAtlasResult.CanvasEdge(
                "relation:" + safeKey(relation.getRelationKey()),
                sourceId,
                targetId,
                relation.getRelationType(),
                relation.getRelationType(),
                confidenceOf(relation.getConfirmationStatus()),
                false);
    }

    private CategorySlot findCategorySlot(String categoryCode) {
        return SANCAI_CATEGORY_SLOTS.stream()
                .filter(slot -> slot.code().equals(categoryCode))
                .findFirst()
                .orElse(null);
    }

    private KnowledgePortalAtlasResult.CategoryEntityHighlight toCategoryEntityHighlight(KnowledgeEntity entity) {
        return new KnowledgePortalAtlasResult.CategoryEntityHighlight(
                String.valueOf(entity.getEntityId()),
                entity.getName(),
                entity.getEntityType(),
                entity.getConfirmationStatus(),
                "/knowledge/atlas?level=detail&entityId=" + entity.getEntityId());
    }

    private KnowledgePortalAtlasQuery normalizeAtlasQuery(KnowledgePortalAtlasQuery query) {
        if (query == null) {
            return new KnowledgePortalAtlasQuery("overview", null, null, null, null, null, null);
        }
        if (query.getLevel() == null || query.getLevel().isBlank()) {
            query.setLevel("overview");
        }
        return query;
    }

    @Override
    public KnowledgePortalQualityResult getQuality() {
        QualityReportDetailResult detail =
                qualityReportApplicationService == null ? null : qualityReportApplicationService.latest(null);
        ReportRecord report = detail == null ? null : detail.getReport();
        if (report == null) {
            return qualityEmptyState();
        }
        return new KnowledgePortalQualityResult(
                List.of(
                        qualityStat(
                                "entity-coverage-rate",
                                "实体覆盖率",
                                toPercent(report.getEntityCoverageRate()),
                                "ratio",
                                "人工确认实体 / 实体总数",
                                rateTone(report.getEntityCoverageRate())),
                        qualityStat(
                                "relation-accuracy-rate",
                                "关系准确率",
                                toPercent(report.getRelationAccuracyRate()),
                                "ratio",
                                "人工确认关系 / 关系总数",
                                rateTone(report.getRelationAccuracyRate())),
                        qualityStat(
                                "lineage-coverage-rate",
                                "世系覆盖率",
                                toPercent(report.getLineageCoverageRate()),
                                "ratio",
                                "人工确认世系 / 世系总数",
                                rateTone(report.getLineageCoverageRate())),
                        qualityStat(
                                "completeness-rate",
                                "完整度",
                                toPercent(report.getCompletenessRate()),
                                "ratio",
                                "确认对象 / 全部治理对象",
                                rateTone(report.getCompletenessRate()))),
                List.of(),
                toSourceBreakdowns(detail.getSourceDetails()),
                toFocusIssues(detail.getIssues()),
                toSourceDetails(detail.getSourceDetails()));
    }

    private KnowledgePortalQualityResult qualityEmptyState() {
        return new KnowledgePortalQualityResult(
                List.of(),
                List.of(),
                List.of(),
                List.of(new KnowledgePortalQualityResult.FocusIssueItem(
                        "尚未生成质量报告", "后台质量报告生成后，Portal 会展示同一份已发布报告快照。", "high", "/knowledge/quality")),
                List.of());
    }

    private List<KnowledgePortalQualityResult.SourceBreakdownItem> toSourceBreakdowns(
            List<SourceDetailRecord> sourceDetails) {
        if (sourceDetails == null || sourceDetails.isEmpty()) {
            return List.of();
        }
        return sourceDetails.stream()
                .map(item -> new KnowledgePortalQualityResult.SourceBreakdownItem(
                        item.getSourceCategoryCode() == null
                                ? item.getSourceContentType()
                                : item.getSourceCategoryCode(),
                        item.getSourceCategoryName() == null
                                ? item.getSourceContentType()
                                : item.getSourceCategoryName(),
                        item.getAnnotationCount(),
                        "质量报告来源明细中的人工标注数量。"))
                .toList();
    }

    private List<KnowledgePortalQualityResult.FocusIssueItem> toFocusIssues(List<IssueRecord> issues) {
        return issues == null
                ? List.of()
                : issues.stream()
                        .map(item -> new KnowledgePortalQualityResult.FocusIssueItem(
                                item.getTitle(), item.getDescription(), item.getSeverity(), item.getHref()))
                        .toList();
    }

    private List<KnowledgePortalQualityResult.SourceDetailItem> toSourceDetails(
            List<SourceDetailRecord> sourceDetails) {
        return sourceDetails == null
                ? List.of()
                : sourceDetails.stream()
                        .map(item -> new KnowledgePortalQualityResult.SourceDetailItem(
                                item.getSourceContentType(),
                                item.getSourceCategoryName() == null
                                        ? item.getSourceContentType()
                                        : item.getSourceCategoryName(),
                                item.getAppliedAt() == null
                                        ? null
                                        : item.getAppliedAt().getTime(),
                                item.getStatus(),
                                item.getHref()))
                        .toList();
    }

    private List<KnowledgePortalHomeResult.PortalRecentUpdateItem> buildRecentUpdates() {
        PageResult<GraphVersion> page = graphVersionRepository.page(
                null, GRAPH_VERSION_APPLIED_STATUS, null, null, FIRST_PAGE_NO, RECENT_UPDATE_LIMIT);
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return List.of(new KnowledgePortalHomeResult.PortalRecentUpdateItem(
                    "等待首批知识版本", "知识门户将在图谱应用后自动展示最近更新", "当前还没有已应用的图谱版本，首页先展示静态导览入口。", null, "/knowledge/atlas", null));
        }
        return page.getRecords().stream().map(this::toRecentUpdate).toList();
    }

    private KnowledgePortalHomeResult.PortalRecentUpdateItem toRecentUpdate(GraphVersion version) {
        String sourceType = version.getSourceContentType() == null ? "UNKNOWN" : version.getSourceContentType();
        String taskType = version.getTaskType() == null ? "GRAPH" : version.getTaskType();
        Long sourceContentId = version.getSourceContentId();
        return new KnowledgePortalHomeResult.PortalRecentUpdateItem(
                sourceType + " · 版本 " + version.getVersionNo(),
                "任务 " + taskType + " / 来源 " + sourceType,
                "已应用版本可继续查看实体关系、来源线索与质量摘要。",
                version.getAppliedAt() == null ? null : version.getAppliedAt().getTime(),
                buildAtlasHref(sourceContentId, sourceType),
                null);
    }

    private String buildAtlasHref(Long sourceContentId, String sourceType) {
        if (sourceContentId == null) {
            return "/knowledge/atlas";
        }
        return "/knowledge/atlas?focusType=" + sourceType + "&focusId=" + sourceContentId;
    }

    private KnowledgePortalHomeResult.PortalStatItem stat(
            String key, String label, long value, String deltaText, String trend, String icon) {
        return new KnowledgePortalHomeResult.PortalStatItem(key, label, String.valueOf(value), deltaText, trend, icon);
    }

    private KnowledgePortalHomeResult.PortalQuickLinkItem quickLink(
            String key, String label, String description, String href, String type) {
        return new KnowledgePortalHomeResult.PortalQuickLinkItem(key, label, description, href, type);
    }

    private KnowledgePortalHomeResult.PortalFeatureCollectionItem featureCollection(
            String key, String label, String description, String href, String badgeText) {
        return new KnowledgePortalHomeResult.PortalFeatureCollectionItem(key, label, description, href, badgeText);
    }

    private GraphVersion latestAppliedVersion() {
        PageResult<GraphVersion> page = graphVersionRepository.page(
                null, GRAPH_VERSION_APPLIED_STATUS, null, null, FIRST_PAGE_NO, COUNT_PAGE_SIZE);
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return null;
        }
        return page.getRecords().get(0);
    }

    private KnowledgePortalAtlasResult.FocusNode toFocusNode(KnowledgeEntity entity) {
        if (entity == null) {
            return null;
        }
        return new KnowledgePortalAtlasResult.FocusNode(
                String.valueOf(entity.getEntityId()),
                entity.getName(),
                entity.getEntityType(),
                entity.getDescription(),
                entity.getConfirmationStatus(),
                confidenceOf(entity.getConfirmationStatus()),
                null);
    }

    private List<KnowledgePortalAtlasResult.RelationGroup> buildRelationGroups(
            KnowledgeEntity focusEntity, List<KnowledgeRelation> relations) {
        if (focusEntity == null || relations == null || relations.isEmpty()) {
            return List.of();
        }
        return relations.stream()
                .filter(relation -> touchesFocusEntity(focusEntity, relation))
                .collect(Collectors.groupingBy(
                        KnowledgeRelation::getRelationType, LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> new KnowledgePortalAtlasResult.RelationGroup(
                        entry.getKey(),
                        entry.getKey(),
                        entry.getValue().stream().map(this::toRelationItem).toList()))
                .toList();
    }

    private List<KnowledgePortalAtlasResult.RelationGroup> buildCategoryRelationGroups(
            List<KnowledgeRelation> relations) {
        if (relations == null || relations.isEmpty()) {
            return List.of();
        }
        return relations.stream()
                .collect(Collectors.groupingBy(
                        relation -> safeKey(relation.getRelationType()),
                        LinkedHashMap::new,
                        Collectors.mapping(this::toRelationItem, Collectors.toList())))
                .entrySet()
                .stream()
                .map(entry ->
                        new KnowledgePortalAtlasResult.RelationGroup(entry.getKey(), entry.getKey(), entry.getValue()))
                .toList();
    }

    private KnowledgePortalAtlasResult.RelationItem toRelationItem(KnowledgeRelation relation) {
        return new KnowledgePortalAtlasResult.RelationItem(
                relation.getSourceEntityKey(),
                relation.getSourceName(),
                relation.getRelationType(),
                relation.getTargetEntityKey(),
                relation.getTargetName(),
                relation.getRelationType(),
                confidenceOf(relation.getConfirmationStatus()));
    }

    private List<KnowledgePortalAtlasResult.SourceReference> buildSourceReferences(GraphVersion latestVersion) {
        if (latestVersion == null) {
            return List.of();
        }
        return List.of(new KnowledgePortalAtlasResult.SourceReference(
                latestVersion.getSourceContentId() == null ? null : String.valueOf(latestVersion.getSourceContentId()),
                latestVersion.getSourceCategoryName() == null
                        ? latestVersion.getSourceContentType()
                        : latestVersion.getSourceCategoryName(),
                latestVersion.getSourceContentType(),
                "当前展示的是最新已应用图谱版本，可继续查看关联实体、来源与时间线。",
                latestVersion.getAppliedAt() == null
                        ? null
                        : latestVersion.getAppliedAt().getTime(),
                "/knowledge/atlas"));
    }

    private List<KnowledgePortalAtlasResult.TimelineItem> buildTimelineItems(KnowledgeEntity focusEntity) {
        if (focusEntity == null) {
            return List.of();
        }
        List<KnowledgePortalAtlasResult.TimelineItem> items = new java.util.ArrayList<>();
        if (focusEntity.getFirstExtractedAt() != null) {
            items.add(new KnowledgePortalAtlasResult.TimelineItem(
                    "首次抽取", "知识首次进入图谱", "该实体在图谱中首次被抽取并登记。", "/knowledge/atlas"));
        }
        if (focusEntity.getLastExtractedAt() != null) {
            items.add(new KnowledgePortalAtlasResult.TimelineItem(
                    "最近抽取", "知识最近一次刷新", "该实体在最近一次图谱应用中被重新刷新。", "/knowledge/atlas"));
        }
        if (focusEntity.getConfirmedAt() != null) {
            items.add(new KnowledgePortalAtlasResult.TimelineItem(
                    "人工确认", "知识已完成人工确认", "该实体已经过人工确认，可用于更稳定的展示。", "/knowledge/atlas"));
        }
        return items;
    }

    private boolean touchesFocusEntity(KnowledgeEntity focusEntity, KnowledgeRelation relation) {
        return Objects.equals(focusEntity.getEntityKey(), relation.getSourceEntityKey())
                || Objects.equals(focusEntity.getEntityKey(), relation.getTargetEntityKey());
    }

    private String safeKey(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value;
    }

    private Double confidenceOf(String confirmationStatus) {
        return "MANUAL_CONFIRMED".equals(confirmationStatus) || "CONFIRMED".equals(confirmationStatus) ? 0.95D : 0.70D;
    }

    private List<String> distinctValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().filter(Objects::nonNull).distinct().toList();
    }

    private List<String> defaultTimeRanges() {
        return List.of("30d", "90d", "all");
    }

    private <T> List<T> defaultList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private List<KnowledgePortalQualityResult.TrendSeries> buildTrendSeries(TagGovernanceMetrics metrics) {
        List<TagGovernanceMetrics.MonthlyNewTagMetric> monthlyNewTags =
                metrics == null ? List.of() : defaultList(metrics.getMonthlyNewTags());
        return List.of(new KnowledgePortalQualityResult.TrendSeries(
                "monthly-new-tags",
                "月度新增标签",
                monthlyNewTags.stream()
                        .map(item -> new KnowledgePortalQualityResult.TrendPoint(item.getMonth(), item.getTagCount()))
                        .toList()));
    }

    private List<KnowledgePortalQualityResult.SourceBreakdownItem> buildSourceBreakdowns(TagGovernanceMetrics metrics) {
        List<TagGovernanceMetrics.SourceRatioMetric> sourceRatios =
                metrics == null ? List.of() : defaultList(metrics.getSourceRatios());
        if (sourceRatios.isEmpty()) {
            return List.of();
        }
        return sourceRatios.stream()
                .map(item -> new KnowledgePortalQualityResult.SourceBreakdownItem(
                        item.getSource() == null ? "UNKNOWN" : item.getSource().name(),
                        item.getSource() == null ? "未知来源" : item.getSource().name(),
                        item.getTagCount(),
                        "标签来源分布，用于理解当前知识资产的治理来源构成。"))
                .toList();
    }

    private List<KnowledgePortalQualityResult.FocusIssueItem> buildFocusIssues(
            GraphVersion latestVersion, double entityConfirmedRate, double relationConfirmedRate, long draftTaskCount) {
        List<KnowledgePortalQualityResult.FocusIssueItem> issues = new java.util.ArrayList<>();
        if (latestVersion == null) {
            issues.add(new KnowledgePortalQualityResult.FocusIssueItem(
                    "缺少已应用图谱版本", "当前还没有可供浏览的已应用知识快照。", "high", "/knowledge/atlas"));
        }
        if (draftTaskCount > 0) {
            issues.add(new KnowledgePortalQualityResult.FocusIssueItem(
                    "存在待处理治理任务", "仍有 refinement 草稿任务尚未进入最终应用，建议优先清理。", "medium", "/knowledge/quality"));
        }
        if (entityConfirmedRate < 0.8D) {
            issues.add(new KnowledgePortalQualityResult.FocusIssueItem(
                    "实体确认率偏低", "当前实体确认率低于 80%，建议优先补齐高频实体确认。", "medium", "/knowledge/atlas"));
        }
        if (relationConfirmedRate < 0.8D) {
            issues.add(new KnowledgePortalQualityResult.FocusIssueItem(
                    "关系确认率偏低", "当前关系确认率低于 80%，建议重点检查核心关系链。", "medium", "/knowledge/atlas"));
        }
        return issues;
    }

    private List<KnowledgePortalQualityResult.SourceDetailItem> buildSourceDetails() {
        PageResult<GraphVersion> recentVersions = graphVersionRepository.page(
                null, GRAPH_VERSION_APPLIED_STATUS, null, null, FIRST_PAGE_NO, RECENT_UPDATE_LIMIT);
        if (recentVersions.getRecords() == null || recentVersions.getRecords().isEmpty()) {
            return List.of();
        }
        return recentVersions.getRecords().stream()
                .map(version -> new KnowledgePortalQualityResult.SourceDetailItem(
                        version.getSourceContentType(),
                        version.getSourceCategoryName() == null
                                ? version.getSourceContentType()
                                : version.getSourceCategoryName(),
                        version.getAppliedAt() == null
                                ? null
                                : version.getAppliedAt().getTime(),
                        version.getStatus(),
                        "/knowledge/atlas"))
                .toList();
    }

    private record CategorySlot(String code, String name) {}

    private KnowledgePortalQualityResult.QualityStatItem qualityStat(
            String key, String label, String value, String unit, String deltaText, String statusTone) {
        return new KnowledgePortalQualityResult.QualityStatItem(key, label, value, unit, deltaText, statusTone);
    }

    private int confirmedEntities(List<KnowledgeEntity> entities) {
        return (int) entities.stream()
                .filter(entity -> "CONFIRMED".equals(entity.getConfirmationStatus())
                        || "MANUAL_CONFIRMED".equals(entity.getConfirmationStatus()))
                .count();
    }

    private int confirmedRelations(List<KnowledgeRelation> relations) {
        return (int) relations.stream()
                .filter(relation -> "CONFIRMED".equals(relation.getConfirmationStatus())
                        || "MANUAL_CONFIRMED".equals(relation.getConfirmationStatus()))
                .count();
    }

    private double ratio(int numerator, int denominator) {
        return denominator <= 0 ? 0D : (double) numerator / denominator;
    }

    private String toPercent(double value) {
        return String.format(Locale.ROOT, "%.0f%%", value * 100D);
    }

    private String toPercent(BigDecimal value) {
        return value == null ? "0%" : String.format(Locale.ROOT, "%.0f%%", value.doubleValue() * 100D);
    }

    private String rateTone(BigDecimal value) {
        return value != null && value.compareTo(new BigDecimal("0.8000")) >= 0 ? "good" : "watch";
    }
}
