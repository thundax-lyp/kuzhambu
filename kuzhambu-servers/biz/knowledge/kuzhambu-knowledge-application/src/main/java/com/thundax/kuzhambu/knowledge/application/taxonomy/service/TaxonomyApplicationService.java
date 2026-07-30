package com.thundax.kuzhambu.knowledge.application.taxonomy.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasRemoveCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchReviewCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagReviewCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagBatchMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagCategoryPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagGovernanceMetricsQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagReviewPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagAliasResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagBatchMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagCategoryResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagDetailResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagExtractionResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagGovernanceMetricsResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import java.util.List;

public interface TaxonomyApplicationService {

    PageResult<TagCategoryResult> pageCategories(TagCategoryPageQuery query, PageQuery page);

    TagCategoryId createCategory(TagCategoryCreateCommand command);

    void updateCategory(TagCategoryUpdateCommand command);

    void changeCategoryStatus(TagCategoryStatusCommand command);

    PageResult<TagResult> pageTags(TagPageQuery query, PageQuery page);

    TagDetailResult getTagDetail(TagId id);

    TagMergePreviewResult previewTagMergeImpact(TagMergePreviewQuery query);

    TagBatchMergePreviewResult previewTagBatchMergeImpact(TagBatchMergePreviewQuery query);

    void applyTagMerge(TagMergeCommand command);

    void applyTagBatchMerge(TagBatchMergeCommand command);

    TagId createTag(TagCreateCommand command);

    void updateTag(TagUpdateCommand command);

    void changeTagStatus(TagStatusCommand command);

    void deprecateTag(TagDeprecateCommand command);

    void batchDeprecateTags(TagBatchDeprecateCommand command);

    TagGovernanceMetricsResult getTagGovernanceMetrics(TagGovernanceMetricsQuery query);

    PageResult<TagResult> pagePendingTags(TagReviewPageQuery query, PageQuery page);

    void reviewTag(TagReviewCommand command);

    void batchReviewTags(TagBatchReviewCommand command);

    TagExtractionResult extractTags(TagExtractionCommand command);

    void applyExtractedTags(TagCandidateApplyCommand command);

    List<TagAliasResult> listTagAliases(TagId tagId);

    TagAliasId createTagAlias(TagAliasCreateCommand command);

    void removeTagAlias(TagAliasRemoveCommand command);
}
