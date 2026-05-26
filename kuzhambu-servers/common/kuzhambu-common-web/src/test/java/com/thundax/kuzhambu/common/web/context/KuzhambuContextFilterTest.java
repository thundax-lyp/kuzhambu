package com.thundax.kuzhambu.common.web.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class KuzhambuContextFilterTest {

    @AfterEach
    public void tearDown() {
        KuzhambuContextHolder.clear();
    }

    @Test
    public void shouldBindAndClearContextForRequest() throws ServletException, IOException {
        KuzhambuContextFilter filter = new KuzhambuContextFilter(new DefaultKuzhambuContextResolver());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(DefaultKuzhambuContextResolver.HEADER_REQUEST_ID, "request-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertEquals("request-1", KuzhambuContextHolder.requestId());
            assertNull(KuzhambuContextHolder.currentToken());
        });

        assertEquals("request-1", response.getHeader(DefaultKuzhambuContextResolver.HEADER_REQUEST_ID));
        assertNull(KuzhambuContextHolder.requestId());
    }

    @Test
    public void shouldTolerateNullResolvedContext() throws ServletException, IOException {
        KuzhambuContextFilter filter = new KuzhambuContextFilter(request -> null);

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (servletRequest, servletResponse) -> assertNull(KuzhambuContextHolder.requestId()));

        assertNull(KuzhambuContextHolder.requestId());
    }

    @Test
    public void shouldLogAndKeepResponseStatus() throws ServletException, IOException {
        KuzhambuContextFilter filter = new KuzhambuContextFilter(new DefaultKuzhambuContextResolver(), 1L);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
        request.addHeader(DefaultKuzhambuContextResolver.HEADER_REQUEST_ID, "request-2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            ((MockHttpServletResponse) servletResponse).setStatus(202);
            assertEquals("request-2", KuzhambuContextHolder.requestId());
        });

        assertEquals(202, response.getStatus());
    }
}
