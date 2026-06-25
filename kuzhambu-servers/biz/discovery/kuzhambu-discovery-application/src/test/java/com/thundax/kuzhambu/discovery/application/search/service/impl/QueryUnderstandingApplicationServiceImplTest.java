package com.thundax.kuzhambu.discovery.application.search.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import org.junit.jupiter.api.Test;

class QueryUnderstandingApplicationServiceImplTest {

    private final QueryUnderstandingApplicationServiceImpl service = new QueryUnderstandingApplicationServiceImpl();

    @Test
    void understandShouldRejectBlankQuery() {
        BizException exception = assertThrows(BizException.class, () -> service.understand(new SearchQuery()));
        assertEquals("Search query is required", exception.getMessage());
    }

    @Test
    void understandShouldKeepNotImplementedBoundary() {
        SearchQuery query = new SearchQuery();
        query.setQueryText("礼制");

        BizException exception = assertThrows(BizException.class, () -> service.understand(query));

        assertEquals("DISCOVERY-20002", exception.getCode());
        assertEquals("Query understanding is not implemented", exception.getMessage());
    }
}
