package com.thundax.kuzhambu.discovery.application.search.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import com.thundax.kuzhambu.discovery.application.search.support.SearchContentProvider;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import java.util.List;
import org.junit.jupiter.api.Test;

class SearchIndexApplicationServiceImplTest {

    @Test
    void rebuildIndexShouldReadPublicContentsAndDelegateToGateway() {
        SearchContentProvider searchContentProvider = mock(SearchContentProvider.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        SearchIndexApplicationServiceImpl service =
                new SearchIndexApplicationServiceImpl(searchContentProvider, searchIndexGateway);
        List<SearchSourceContent> sourceContents = List.of(new SearchSourceContent(
                "CLASSICS",
                "SANCAI_ENTRY",
                "1001",
                "SANCAI_ENTRY",
                "11",
                "天文",
                "黄帝",
                "摘要",
                List.of("原文"),
                List.of(),
                "PUBLISHED",
                "PUBLIC",
                null,
                null));
        when(searchContentProvider.listPublicContents()).thenReturn(sourceContents);

        Integer rebuiltCount = service.rebuildIndex();

        verify(searchIndexGateway).rebuildIndex(sourceContents);
        assertEquals(1, rebuiltCount);
    }
}
