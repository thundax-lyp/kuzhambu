package com.thundax.kuzhambu.knowledge.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.application.facade.assembler.KnowledgeFacadeAssembler;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult;
import com.thundax.kuzhambu.knowledge.application.report.service.KnowledgeReportApplicationService;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryEntityHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryTagHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.KnowledgeTaxonomyReadApplicationService;
import com.thundax.kuzhambu.knowledge.domain.service.KnowledgeTagBindingDomainService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeContentTagRefFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeDiscoveryTermFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeRemoveContentTagRefFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeResolveTagFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeSummaryFacadeRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeFacadeImplTest {

    @Test
    void summaryShouldDelegateAndMapFacadeResponse() {
        KnowledgeReportApplicationService knowledgeReportApplicationService =
                mock(KnowledgeReportApplicationService.class);
        Date periodStart = new Date(1_735_689_600_000L);
        Date periodEnd = new Date(1_735_776_000_000L);
        when(knowledgeReportApplicationService.summary(periodStart, periodEnd, "WEEK"))
                .thenReturn(new KnowledgeReportSummaryResult(
                        periodStart,
                        periodEnd,
                        new BigDecimal("0.8750"),
                        List.of(new KnowledgeReportSummaryResult.TopTagResult("礼制", 12L)),
                        List.of(new KnowledgeReportSummaryResult.CategoryDistributionResult("礼学", 8L)),
                        List.of(new KnowledgeReportSummaryResult.MonthlyNewTagResult("2025-01", 3L))));
        KnowledgeFacadeImpl facade = newFacade(
                knowledgeReportApplicationService,
                mock(KnowledgeTaxonomyReadApplicationService.class),
                mock(KnowledgeTagBindingDomainService.class));

        var response = facade.summary(KnowledgeSummaryFacadeRequest.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .bucketType("WEEK")
                .build());

        assertEquals(periodStart, response.getPeriodStart());
        assertEquals(periodEnd, response.getPeriodEnd());
        assertEquals(new BigDecimal("0.8750"), response.getTagCoverageRate());
        assertEquals("礼制", response.getTopTags().get(0).getTagName());
        assertEquals(12L, response.getTopTags().get(0).getContentRefCount());
        assertEquals("礼学", response.getCategoryDistributions().get(0).getCategoryName());
        assertEquals(8L, response.getCategoryDistributions().get(0).getTagCount());
        assertEquals("2025-01", response.getMonthlyNewTags().get(0).getBucket());
        assertEquals(3L, response.getMonthlyNewTags().get(0).getTagCount());
    }

    @Test
    void discoveryReadMethodsShouldDelegateAndMapFacadeResponses() {
        KnowledgeTaxonomyReadApplicationService knowledgeTaxonomyReadApplicationService =
                mock(KnowledgeTaxonomyReadApplicationService.class);
        when(knowledgeTaxonomyReadApplicationService.getTagHint("礼制"))
                .thenReturn(new DiscoveryTagHintResult("礼制", "礼制", "礼制", null, 6L));
        when(knowledgeTaxonomyReadApplicationService.listEntityHints("礼制"))
                .thenReturn(List.of(new DiscoveryEntityHintResult("礼制", "礼制", "周礼", "BOOK", 2L)));
        KnowledgeFacadeImpl facade = newFacade(
                mock(KnowledgeReportApplicationService.class),
                knowledgeTaxonomyReadApplicationService,
                mock(KnowledgeTagBindingDomainService.class));
        KnowledgeDiscoveryTermFacadeRequest request =
                KnowledgeDiscoveryTermFacadeRequest.builder().term("礼制").build();

        var tagHint = facade.getTagHint(request);
        var entityHints = facade.listEntityHints(request);

        assertEquals("礼制", tagHint.getMatchedTagName());
        assertEquals(6L, tagHint.getContentRefCount());
        assertEquals(1, entityHints.getEntityHints().size());
        assertEquals("周礼", entityHints.getEntityHints().get(0).getEntityName());
        assertEquals("BOOK", entityHints.getEntityHints().get(0).getEntityType());
    }

    @Test
    void resolveTagShouldMapFacadeResponse() {
        KnowledgeTagBindingDomainService knowledgeTagBindingDomainService =
                mock(KnowledgeTagBindingDomainService.class);
        Tag tag = new Tag();
        tag.setId(TagIdCodec.toDomain(9L));
        tag.setName("礼制");
        when(knowledgeTagBindingDomainService.resolveOrCreateManualTag("礼制")).thenReturn(tag);
        when(knowledgeTagBindingDomainService.resolveOrCreateAiTag("礼制")).thenReturn(tag);
        KnowledgeFacadeImpl facade = newFacade(
                mock(KnowledgeReportApplicationService.class),
                mock(KnowledgeTaxonomyReadApplicationService.class),
                knowledgeTagBindingDomainService);
        KnowledgeResolveTagFacadeRequest request =
                KnowledgeResolveTagFacadeRequest.builder().tagName("礼制").build();

        var manualResponse = facade.resolveOrCreateManualTag(request);
        var aiResponse = facade.resolveOrCreateAiTag(request);

        assertEquals(9L, manualResponse.getTagId());
        assertEquals("礼制", manualResponse.getTagName());
        assertEquals(9L, aiResponse.getTagId());
        assertEquals("礼制", aiResponse.getTagName());
    }

    @Test
    void syncAndRemoveShouldTranslateFacadeRequestsToDomainArguments() {
        KnowledgeTagBindingDomainService knowledgeTagBindingDomainService =
                mock(KnowledgeTagBindingDomainService.class);
        KnowledgeFacadeImpl facade = newFacade(
                mock(KnowledgeReportApplicationService.class),
                mock(KnowledgeTaxonomyReadApplicationService.class),
                knowledgeTagBindingDomainService);

        facade.syncContentTagRef(KnowledgeContentTagRefFacadeRequest.builder()
                .tagId(12L)
                .contentType("MING_CUSTOMS")
                .contentId(34L)
                .contentTitle("大明会典")
                .tagSource("AI")
                .build());
        facade.removeContentTagRef(KnowledgeRemoveContentTagRefFacadeRequest.builder()
                .tagId(12L)
                .contentType("MING_CUSTOMS")
                .contentId(34L)
                .build());

        ArgumentCaptor<TagId> tagIdCaptor = ArgumentCaptor.forClass(TagId.class);
        ArgumentCaptor<ContentType> contentTypeCaptor = ArgumentCaptor.forClass(ContentType.class);
        ArgumentCaptor<Long> contentIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> contentTitleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TagSource> tagSourceCaptor = ArgumentCaptor.forClass(TagSource.class);
        verify(knowledgeTagBindingDomainService)
                .syncContentTagRef(
                        tagIdCaptor.capture(),
                        contentTypeCaptor.capture(),
                        contentIdCaptor.capture(),
                        contentTitleCaptor.capture(),
                        tagSourceCaptor.capture());
        assertEquals(12L, tagIdCaptor.getValue().value());
        assertEquals(ContentType.MING_CUSTOM, contentTypeCaptor.getValue());
        assertEquals(34L, contentIdCaptor.getValue());
        assertEquals("大明会典", contentTitleCaptor.getValue());
        assertEquals(TagSource.AI_EXTRACTED, tagSourceCaptor.getValue());

        ArgumentCaptor<TagId> removeTagIdCaptor = ArgumentCaptor.forClass(TagId.class);
        ArgumentCaptor<ContentType> removeContentTypeCaptor = ArgumentCaptor.forClass(ContentType.class);
        ArgumentCaptor<Long> removeContentIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(knowledgeTagBindingDomainService)
                .removeContentTagRef(
                        removeTagIdCaptor.capture(),
                        removeContentTypeCaptor.capture(),
                        removeContentIdCaptor.capture());
        assertEquals(12L, removeTagIdCaptor.getValue().value());
        assertEquals(ContentType.MING_CUSTOM, removeContentTypeCaptor.getValue());
        assertEquals(34L, removeContentIdCaptor.getValue());
    }

    @Test
    void nullRequestsShouldKeepFacadeBoundaryStable() {
        KnowledgeReportApplicationService knowledgeReportApplicationService =
                mock(KnowledgeReportApplicationService.class);
        KnowledgeTaxonomyReadApplicationService knowledgeTaxonomyReadApplicationService =
                mock(KnowledgeTaxonomyReadApplicationService.class);
        KnowledgeTagBindingDomainService knowledgeTagBindingDomainService =
                mock(KnowledgeTagBindingDomainService.class);
        KnowledgeFacadeImpl facade = newFacade(
                knowledgeReportApplicationService,
                knowledgeTaxonomyReadApplicationService,
                knowledgeTagBindingDomainService);

        assertEquals(null, facade.summary(null));
        assertEquals(null, facade.getTagHint(null));
        assertEquals(null, facade.listEntityHints(null));
        assertEquals(null, facade.resolveOrCreateManualTag(null));
        assertEquals(null, facade.resolveOrCreateAiTag(null));
        facade.syncContentTagRef(null);
        facade.removeContentTagRef(null);

        verifyNoInteractions(
                knowledgeReportApplicationService,
                knowledgeTaxonomyReadApplicationService,
                knowledgeTagBindingDomainService);
    }

    private KnowledgeFacadeImpl newFacade(
            KnowledgeReportApplicationService knowledgeReportApplicationService,
            KnowledgeTaxonomyReadApplicationService knowledgeTaxonomyReadApplicationService,
            KnowledgeTagBindingDomainService knowledgeTagBindingDomainService) {
        return new KnowledgeFacadeImpl(
                knowledgeReportApplicationService,
                knowledgeTaxonomyReadApplicationService,
                knowledgeTagBindingDomainService,
                new KnowledgeFacadeAssembler());
    }
}
