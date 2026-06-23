package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.TaxonomyApplicationService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.SynonymRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagCategoryRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import org.junit.jupiter.api.Test;

class TaxonomyApplicationServiceImplTest {

    @Test
    void previewTagMergeImpactShouldExposeStableContractBeforeReadModel() {
        TaxonomyApplicationService service = new TaxonomyApplicationServiceImpl(
                mock(TagCategoryRepository.class),
                mock(TagRepository.class),
                mock(TagAliasRepository.class),
                mock(TagContentRefRepository.class),
                mock(SynonymRepository.class));

        BizException error = assertThrows(
                BizException.class,
                () -> service.previewTagMergeImpact(new TagMergePreviewQuery(TagId.of(1L), TagId.of(2L))));

        assertEquals("标签合并影响预览读模型尚未实现", error.getMessage());
    }
}
