package com.thundax.kuzhambu.discovery.interfaces.portal.search.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.discovery.application.search.command.SearchClickCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchClickRequest;
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
    void toCommandShouldFillAnonymousRequestContext() {
        DiscoverySearchClickRequest request = new DiscoverySearchClickRequest();
        request.setSearchLogId("search-1");
        request.setContentDomain("CLASSICS");
        request.setContentType("MING_CUSTOMS");
        request.setContentId("content-1");
        request.setResultGroupKey("MING_CUSTOMS");
        request.setResultRank(1);
        request.setGroupRank(1);

        SearchClickCreateCommand command = DiscoverySearchPortalInterfaceAssembler.toCommand(request);

        assertEquals("ANONYMOUS", command.getOperatorType());
        assertNotNull(command.getRequestId());
        assertNotNull(command.getTraceId());
    }
}
