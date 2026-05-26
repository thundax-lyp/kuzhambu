package com.thundax.kuzhambu.common.web.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class DefaultKuzhambuContextResolverTest {

    private final DefaultKuzhambuContextResolver resolver = new DefaultKuzhambuContextResolver();

    @Test
    public void shouldResolveContextFromHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(DefaultKuzhambuContextResolver.HEADER_REQUEST_ID, "request-1");

        String requestId = resolver.resolveRequestId(request);

        assertEquals("request-1", requestId);
    }

    @Test
    public void shouldGenerateRequestIdWhenHeaderMissing() {
        String requestId = resolver.resolveRequestId(new MockHttpServletRequest());

        assertNotNull(requestId);
    }
}
