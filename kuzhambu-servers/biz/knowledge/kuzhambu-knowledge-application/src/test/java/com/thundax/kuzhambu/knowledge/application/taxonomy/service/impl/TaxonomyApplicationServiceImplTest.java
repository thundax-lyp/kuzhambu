package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.TaxonomyApplicationService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagContentRef;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagContentRefId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.SynonymRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagCategoryRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaxonomyApplicationServiceImplTest {

    @Test
    void previewTagMergeImpactShouldAggregateAliasesContentRefsAndPendingReview() {
        TagCategoryRepository categoryRepository = mock(TagCategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        TagAliasRepository aliasRepository = mock(TagAliasRepository.class);
        TagContentRefRepository contentRefRepository = mock(TagContentRefRepository.class);
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                categoryRepository,
                tagRepository,
                aliasRepository,
                contentRefRepository,
                mock(SynonymRepository.class));

        Tag sourceTag = new Tag(
                TagId.of(1L),
                TagId.of(1L),
                "源标签",
                TagCategoryId.of(11L),
                "source",
                TagStatus.ENABLED,
                TagSource.AI_EXTRACTED,
                TagReviewStatus.PENDING,
                null,
                null,
                null);
        Tag targetTag = new Tag(
                TagId.of(2L),
                TagId.of(2L),
                "目标标签",
                TagCategoryId.of(12L),
                "target",
                TagStatus.ENABLED,
                TagSource.MANUAL,
                TagReviewStatus.APPROVED,
                null,
                null,
                null);
        when(tagRepository.getByTagId(TagId.of(1L))).thenReturn(sourceTag);
        when(tagRepository.getByTagId(TagId.of(2L))).thenReturn(targetTag);
        when(categoryRepository.getByCategoryId(TagCategoryId.of(11L)))
                .thenReturn(new TagCategory(TagCategoryId.of(11L), TagCategoryId.of(11L), "源分类", null, 1, null));
        when(categoryRepository.getByCategoryId(TagCategoryId.of(12L)))
                .thenReturn(new TagCategory(TagCategoryId.of(12L), TagCategoryId.of(12L), "目标分类", null, 1, null));
        when(aliasRepository.listByTagId(TagId.of(1L)))
                .thenReturn(List.of(
                        new TagAlias(TagAliasId.of(21L), TagAliasId.of(21L), TagId.of(1L), "别名一", TagSource.MANUAL)));
        when(contentRefRepository.listByTagId(TagId.of(1L)))
                .thenReturn(List.of(new TagContentRef(
                        TagContentRefId.of(31L),
                        TagContentRefId.of(31L),
                        TagId.of(1L),
                        ContentType.SANCAI_ENTRY,
                        1001L,
                        "内容一",
                        TagSource.AI_EXTRACTED)));
        when(contentRefRepository.countByTagId(TagId.of(2L))).thenReturn(2);

        TagMergePreviewResult result =
                service.previewTagMergeImpact(new TagMergePreviewQuery(TagId.of(1L), TagId.of(2L)));

        assertEquals("源标签", result.getSourceTag().getName());
        assertEquals("目标标签", result.getTargetTag().getName());
        assertEquals("源分类", result.getSourceTag().getCategoryName());
        assertEquals("目标分类", result.getTargetTag().getCategoryName());
        assertEquals(1, result.getAliasesToMerge().size());
        assertEquals("别名一", result.getAliasesToMerge().get(0).getName());
        assertEquals(1, result.getImpactedContentRefs().size());
        assertEquals("内容一", result.getImpactedContentRefs().get(0).getContentTitle());
        assertEquals(1, result.getPendingReviewCount());
        assertEquals(3, result.getGovernedRecordCount());
    }
}
