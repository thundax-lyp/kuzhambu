package com.thundax.kuzhambu.knowledge.infra.taxonomy.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagGovernanceMetricsRepository;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.assembler.TaxonomyPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagContentRefDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagCategoryMapper;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagContentRefMapper;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class TagGovernanceMetricsRepositoryImpl implements TagGovernanceMetricsRepository {

    private static final int DEFAULT_TOP_LIMIT = 10;
    private static final int DEFAULT_RECENT_MONTHS = 6;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TagMapper tagMapper;
    private final TagCategoryMapper tagCategoryMapper;
    private final TagContentRefMapper tagContentRefMapper;

    public TagGovernanceMetricsRepositoryImpl(
            TagMapper tagMapper, TagCategoryMapper tagCategoryMapper, TagContentRefMapper tagContentRefMapper) {
        this.tagMapper = tagMapper;
        this.tagCategoryMapper = tagCategoryMapper;
        this.tagContentRefMapper = tagContentRefMapper;
    }

    @Override
    public TagGovernanceMetrics getMetrics(Integer topLimit, Integer recentMonths) {
        int effectiveTopLimit = normalizePositive(topLimit, DEFAULT_TOP_LIMIT);
        int effectiveRecentMonths = normalizePositive(recentMonths, DEFAULT_RECENT_MONTHS);

        List<Tag> activeTags = TaxonomyPersistenceAssembler.toTagDomainList(tagMapper.selectList(activeTagQuery()));
        List<Tag> approvedTags = TaxonomyPersistenceAssembler.toTagDomainList(tagMapper.selectList(approvedTagQuery()));
        List<TagCategory> categories = TaxonomyPersistenceAssembler.toTagCategoryDomainList(
                tagCategoryMapper.selectList(new QueryWrapper<>()));
        List<TagContentRefDO> contentRefs = tagContentRefMapper.selectList(new QueryWrapper<>());

        return new TagGovernanceMetrics(
                buildTopTags(activeTags, contentRefs, effectiveTopLimit),
                buildCategoryDistributions(activeTags, categories),
                buildSourceRatios(activeTags),
                buildMonthlyNewTags(approvedTags, effectiveRecentMonths));
    }

    @Override
    public BigDecimal getTagCoverageRate() {
        List<Tag> activeTags = TaxonomyPersistenceAssembler.toTagDomainList(tagMapper.selectList(activeTagQuery()));
        if (activeTags.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<TagContentRefDO> contentRefs = tagContentRefMapper.selectList(new QueryWrapper<>());
        Map<Long, Long> contentRefCountByTagId = contentRefs.stream()
                .filter(Objects::nonNull)
                .filter(ref -> ref.getTagId() != null)
                .collect(Collectors.groupingBy(TagContentRefDO::getTagId, Collectors.counting()));
        long coveredTagCount = activeTags.stream()
                .filter(Objects::nonNull)
                .filter(tag -> tag.getTagId() != null)
                .filter(tag ->
                        contentRefCountByTagId.getOrDefault(tag.getTagId().value(), 0L) > 0)
                .count();
        return BigDecimal.valueOf(coveredTagCount)
                .divide(BigDecimal.valueOf(activeTags.size()), 4, RoundingMode.HALF_UP);
    }

    private QueryWrapper<TagDO> activeTagQuery() {
        return new QueryWrapper<TagDO>()
                .eq("status", TagStatus.ENABLED.value())
                .eq("review_status", TagReviewStatus.APPROVED.value())
                .isNull("merged_to_tag_id")
                .isNull("deprecated_at");
    }

    private QueryWrapper<TagDO> approvedTagQuery() {
        return new QueryWrapper<TagDO>().eq("review_status", TagReviewStatus.APPROVED.value());
    }

    private List<TagGovernanceMetrics.TagUsageMetric> buildTopTags(
            List<Tag> activeTags, List<TagContentRefDO> contentRefs, int topLimit) {
        Map<Long, Long> contentRefCountByTagId = contentRefs.stream()
                .filter(Objects::nonNull)
                .filter(ref -> ref.getTagId() != null)
                .collect(Collectors.groupingBy(TagContentRefDO::getTagId, Collectors.counting()));

        return activeTags.stream()
                .filter(Objects::nonNull)
                .map(tag -> new TagGovernanceMetrics.TagUsageMetric(
                        tag.getName(),
                        contentRefCountByTagId.getOrDefault(tag.getTagId().value(), 0L)))
                .sorted(Comparator.comparing(TagGovernanceMetrics.TagUsageMetric::getContentRefCount)
                        .reversed()
                        .thenComparing(
                                TagGovernanceMetrics.TagUsageMetric::getTagName,
                                Comparator.nullsLast(String::compareTo)))
                .limit(topLimit)
                .collect(Collectors.toList());
    }

    private List<TagGovernanceMetrics.CategoryDistributionMetric> buildCategoryDistributions(
            List<Tag> activeTags, List<TagCategory> categories) {
        Map<Long, String> categoryNameById = categories.stream()
                .filter(Objects::nonNull)
                .filter(category -> category.getCategoryId() != null)
                .collect(Collectors.toMap(
                        category -> category.getCategoryId().value(), TagCategory::getName, (left, right) -> left));

        return activeTags.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        tag -> tag.getCategoryId() == null
                                ? 0L
                                : tag.getCategoryId().value(),
                        Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new TagGovernanceMetrics.CategoryDistributionMetric(
                        categoryNameById.getOrDefault(entry.getKey(), "未分类"), entry.getValue()))
                .sorted(Comparator.comparing(TagGovernanceMetrics.CategoryDistributionMetric::getTagCount)
                        .reversed()
                        .thenComparing(
                                TagGovernanceMetrics.CategoryDistributionMetric::getCategoryName,
                                Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    private List<TagGovernanceMetrics.SourceRatioMetric> buildSourceRatios(List<Tag> activeTags) {
        return activeTags.stream()
                .filter(Objects::nonNull)
                .filter(tag -> tag.getSource() != null)
                .collect(Collectors.groupingBy(Tag::getSource, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new TagGovernanceMetrics.SourceRatioMetric(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(TagGovernanceMetrics.SourceRatioMetric::getTagCount)
                        .reversed()
                        .thenComparing(metric -> metric.getSource().ordinal()))
                .collect(Collectors.toList());
    }

    private List<TagGovernanceMetrics.MonthlyNewTagMetric> buildMonthlyNewTags(
            List<Tag> approvedTags, int recentMonths) {
        Map<YearMonth, Long> countByMonth = approvedTags.stream()
                .filter(Objects::nonNull)
                .map(this::resolveUsableMonth)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return countByMonth.entrySet().stream()
                .sorted(Map.Entry.<YearMonth, Long>comparingByKey().reversed())
                .limit(recentMonths)
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new TagGovernanceMetrics.MonthlyNewTagMetric(
                        MONTH_FORMATTER.format(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
    }

    private YearMonth resolveUsableMonth(Tag tag) {
        if (tag.getReviewedAt() != null) {
            return YearMonth.from(tag.getReviewedAt().toInstant().atZone(ZoneId.systemDefault()));
        }
        if (tag.getCreatedAt() != null) {
            return YearMonth.from(tag.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()));
        }
        return null;
    }

    private int normalizePositive(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }
}
