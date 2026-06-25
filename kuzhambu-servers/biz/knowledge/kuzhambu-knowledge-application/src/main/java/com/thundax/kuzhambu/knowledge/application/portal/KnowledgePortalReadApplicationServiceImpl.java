package com.thundax.kuzhambu.knowledge.application.portal;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
    private static final String GRAPH_VERSION_APPLIED_STATUS = "APPLIED";

    private final TagRepository tagRepository;
    private final GraphVersionRepository graphVersionRepository;
    private final KnowledgeEntityRepository knowledgeEntityRepository;
    private final KnowledgeRelationRepository knowledgeRelationRepository;

    public KnowledgePortalReadApplicationServiceImpl(
            TagRepository tagRepository,
            GraphVersionRepository graphVersionRepository,
            KnowledgeEntityRepository knowledgeEntityRepository,
            KnowledgeRelationRepository knowledgeRelationRepository) {
        this.tagRepository = tagRepository;
        this.graphVersionRepository = graphVersionRepository;
        this.knowledgeEntityRepository = knowledgeEntityRepository;
        this.knowledgeRelationRepository = knowledgeRelationRepository;
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
    public KnowledgePortalAtlasResult getAtlas() {
        GraphVersion latestVersion = latestAppliedVersion();
        if (latestVersion == null || latestVersion.getVersionId() == null) {
            return new KnowledgePortalAtlasResult(
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    new KnowledgePortalAtlasResult.AvailableFilters(
                            List.of(), List.of(), List.of(), List.of(), defaultTimeRanges()));
        }
        List<KnowledgeEntity> entities =
                defaultList(knowledgeEntityRepository.listByVersionId(latestVersion.getVersionId()));
        List<KnowledgeRelation> relations =
                defaultList(knowledgeRelationRepository.listByVersionId(latestVersion.getVersionId()));
        KnowledgeEntity focusEntity = entities.isEmpty() ? null : entities.get(0);
        return new KnowledgePortalAtlasResult(
                toFocusNode(focusEntity),
                buildRelationGroups(focusEntity, relations),
                buildSourceReferences(latestVersion),
                List.of(),
                buildTimelineItems(focusEntity),
                new KnowledgePortalAtlasResult.AvailableFilters(
                        distinctValues(List.of(latestVersion.getSourceContentType())),
                        distinctValues(entities.stream()
                                .map(KnowledgeEntity::getEntityType)
                                .toList()),
                        distinctValues(relations.stream()
                                .map(KnowledgeRelation::getRelationType)
                                .toList()),
                        List.of(),
                        defaultTimeRanges()));
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
}
