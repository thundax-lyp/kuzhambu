package com.thundax.kuzhambu.classics.application.wangqi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.application.wangqi.service.impl.WangqiDocumentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import org.junit.jupiter.api.Test;

class WangqiDocumentApplicationServiceImplTest {

    @Test
    void listTimelineShouldPassKeywordVisibilityAndSortDirection() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        WangqiDocumentApplicationServiceImpl service =
                new WangqiDocumentApplicationServiceImpl(repository, contentApplicationService);
        WangqiDocument document = new WangqiDocument();
        when(repository.listTimeline("山川", "PUBLIC", SortDirection.DESC)).thenReturn(List.of(document));

        List<WangqiDocument> result = service.listTimeline(
                new WangqiDocumentPageQuery("山川", WangqiDocumentVisibility.PUBLIC, SortDirection.DESC));

        assertEquals(List.of(document), result);
        verify(repository).listTimeline("山川", "PUBLIC", SortDirection.DESC);
    }
}
