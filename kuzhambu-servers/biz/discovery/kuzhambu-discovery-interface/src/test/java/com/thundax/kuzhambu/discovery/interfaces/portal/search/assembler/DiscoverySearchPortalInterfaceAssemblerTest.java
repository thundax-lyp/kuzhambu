package com.thundax.kuzhambu.discovery.interfaces.portal.search.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.discovery.application.search.command.SearchClickEventCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPreviewQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchClickEventRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchPreviewRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchRequest;
import org.junit.jupiter.api.Test;

class DiscoverySearchPortalInterfaceAssemblerTest {

    @Test
    void toQueryShouldFillAnonymousRequestContext() {
        DiscoverySearchRequest request = new DiscoverySearchRequest();
        request.setQueryText("礼制");

        SearchQuery query = DiscoverySearchPortalInterfaceAssembler.toQuery(request);

        assertEquals("ANONYMOUS", query.getOperatorType());
        assertNotNull(query.getRequestId());
        assertNotNull(query.getTraceId());
    }

    @Test
    void toQueryShouldNormalizeNullQueryTextAndIgnoreInvalidDateFilters() {
        DiscoverySearchRequest request = new DiscoverySearchRequest();
        request.setQueryText(null);
        request.setDateFrom("not-a-date");
        request.setDateTo("also-not-a-date");

        SearchQuery query = DiscoverySearchPortalInterfaceAssembler.toQuery(request);

        assertEquals("", query.getQueryText());
        assertNull(query.getDateFrom());
        assertNull(query.getDateTo());
    }

    @Test
    void toCommandShouldFillAnonymousRequestContext() {
        DiscoverySearchClickEventRequest request = new DiscoverySearchClickEventRequest();
        request.setSearchEventId("search-1");
        request.setContentDomain("CLASSICS");
        request.setContentType("MING_CUSTOMS");
        request.setContentId("content-1");
        request.setResultGroupKey("MING_CUSTOMS");
        request.setResultRank(1);
        request.setGroupRank(1);

        SearchClickEventCreateCommand command = DiscoverySearchPortalInterfaceAssembler.toCommand(request);

        assertEquals("ANONYMOUS", command.getOperatorType());
        assertNotNull(command.getRequestId());
        assertNotNull(command.getTraceId());
    }

    @Test
    void toPreviewQueryShouldFillAnonymousRequestContext() {
        DiscoverySearchPreviewRequest request = new DiscoverySearchPreviewRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setContentId("1001");

        SearchPreviewQuery query = DiscoverySearchPortalInterfaceAssembler.toQuery(request);

        assertEquals("SANCAI_ENTRY", query.getContentType());
        assertEquals("1001", query.getContentId());
        assertEquals("ANONYMOUS", query.getOperatorType());
        assertNotNull(query.getRequestId());
        assertNotNull(query.getTraceId());
    }
}
