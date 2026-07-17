package com.thundax.kuzhambu.classics.application.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.search.service.impl.ClassicsSearchContentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsSearchContentApplicationServiceImplTest {

    @Test
    void listPublicContentsShouldAggregateThreeClassicsSources() {
        SancaiApplicationService sancaiApplicationService = mock(SancaiApplicationService.class);
        WangqiDocumentApplicationService wangqiDocumentApplicationService =
                mock(WangqiDocumentApplicationService.class);
        MingCustomsApplicationService mingCustomsApplicationService = mock(MingCustomsApplicationService.class);
        ClassicsContentApplicationService classicsContentApplicationService =
                mock(ClassicsContentApplicationService.class);
        ClassicsSearchContentApplicationServiceImpl service = new ClassicsSearchContentApplicationServiceImpl(
                sancaiApplicationService,
                wangqiDocumentApplicationService,
                mingCustomsApplicationService,
                classicsContentApplicationService);

        SancaiCategory category = new SancaiCategory(SancaiCategoryId.of(11L), "天文", null, 1);
        SancaiVolume volume = new SancaiVolume(SancaiVolumeId.of(22L), SancaiCategoryId.of(11L), "卷一", null, 1);
        SancaiEntry sancaiEntry = new SancaiEntry();
        sancaiEntry.setId(SancaiEntryId.of(1001L));
        sancaiEntry.setVolumeId(SancaiVolumeId.of(22L));
        sancaiEntry.setTitle("黄帝");
        sancaiEntry.setOriginalText("原文");
        sancaiEntry.setTranslationText("译文");
        sancaiEntry.setSummary("摘要");
        sancaiEntry.setLifecycleStatus(SancaiEntryLifecycleStatus.PUBLISHED);
        sancaiEntry.setVisibility(SancaiEntryVisibility.PUBLIC);
        sancaiEntry.setContentUpdatedAt(new Date(1_718_000_000_000L));
        when(sancaiApplicationService.listCategories()).thenReturn(List.of(category));
        when(sancaiApplicationService.listVolumes(SancaiCategoryId.of(11L))).thenReturn(List.of(volume));
        when(sancaiApplicationService.listEntries(org.mockito.ArgumentMatchers.any(SancaiEntryPageQuery.class)))
                .thenReturn(List.of(sancaiEntry));

        WangqiDocument wangqiDocument = new WangqiDocument();
        wangqiDocument.setId(WangqiDocumentId.of(2001L));
        wangqiDocument.setTitle("天工");
        wangqiDocument.setSummary("王圻摘要");
        wangqiDocument.setContent("正文");
        wangqiDocument.setVisibility(WangqiDocumentVisibility.PUBLIC);
        wangqiDocument.setDocumentTime(new Date(1_718_100_000_000L));
        wangqiDocument.setContentUpdatedAt(new Date(1_718_200_000_000L));
        when(wangqiDocumentApplicationService.listTimeline(
                        org.mockito.ArgumentMatchers.any(WangqiDocumentPageQuery.class)))
                .thenReturn(List.of(wangqiDocument));

        MingCustomsEntry mingEntry = new MingCustomsEntry();
        mingEntry.setId(MingCustomsEntryId.of(3001L));
        mingEntry.setCategory("节令");
        mingEntry.setTitle("元旦");
        mingEntry.setSummary("节令摘要");
        mingEntry.setContent("节令正文");
        mingEntry.setOriginalExcerpts("原文摘录");
        mingEntry.setVisibility(MingCustomsVisibility.PUBLIC);
        mingEntry.setContentUpdatedAt(new Date(1_718_300_000_000L));
        when(mingCustomsApplicationService.page(
                        org.mockito.ArgumentMatchers.any(MingCustomsPageQuery.class),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(PageResult.of(1, 200, 1, List.of(mingEntry)))
                .thenReturn(PageResult.of(2, 200, 1, List.of()));
        when(mingCustomsApplicationService.listKeywords(MingCustomsEntryId.of(3001L)))
                .thenReturn(List.of(
                        new MingCustomsKeyword(MingCustomsKeywordId.of(9001L), MingCustomsEntryId.of(3001L), "元日", 1)));
        when(classicsContentApplicationService.listTags(
                        ClassicsContentType.SANCAI_ENTRY.value(), ClassicsContentId.of(1001L)))
                .thenReturn(List.of(new ClassicsContentTag(
                        null,
                        ClassicsContentType.SANCAI_ENTRY,
                        ClassicsContentId.of(1001L),
                        null,
                        "礼制",
                        ClassicsContentSource.MANUAL,
                        ClassicsContentTagStatus.ACTIVE,
                        1)));
        when(classicsContentApplicationService.listQaPairs(
                        ClassicsContentType.SANCAI_ENTRY.value(), ClassicsContentId.of(1001L)))
                .thenReturn(List.of(new ClassicsContentQaPair(
                        null,
                        ClassicsContentType.SANCAI_ENTRY,
                        ClassicsContentId.of(1001L),
                        "黄帝是谁？",
                        "黄帝是上古帝王。",
                        ClassicsContentSource.MANUAL,
                        1)));

        var results = service.listPublicContents();

        assertEquals(3, results.size());
        assertEquals("天文", results.get(0).getCategoryName());
        assertEquals("PUBLISHED", results.get(1).getStatus());
        assertEquals("节令", results.get(2).getCategoryCode());
        assertTrue(results.get(0).getTextSegments().contains("原文"));
        assertTrue(results.get(0).getTextSegments().contains("黄帝是谁？"));
        assertTrue(results.get(0).getTagNames().contains("礼制"));
        assertTrue(results.get(2).getTextSegments().contains("元日"));
    }
}
