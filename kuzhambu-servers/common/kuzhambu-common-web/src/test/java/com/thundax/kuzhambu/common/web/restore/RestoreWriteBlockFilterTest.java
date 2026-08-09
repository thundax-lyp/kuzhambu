package com.thundax.kuzhambu.common.web.restore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.web.configure.RestoreWriteBlockProperties;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class RestoreWriteBlockFilterTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void shouldBlockBusinessWriteRequest() throws ServletException, IOException {
        RestoreWriteBlockState state = blockedState();
        RestoreWriteBlockFilter filter = filter(state, new RestoreWriteBlockProperties());
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain chain = new CountingFilterChain();

        filter.doFilter(new MockHttpServletRequest("POST", "/api/classics/sancai/save"), response, chain);

        assertFalse(chain.invoked);
        assertEquals(HttpStatus.LOCKED.value(), response.getStatus());
        JsonNode body = OBJECT_MAPPER.readTree(response.getContentAsString());
        assertEquals(
                RestoreWriteBlockFilter.WRITE_BLOCKED_CODE, body.get("code").asText());
        assertEquals(
                RestoreWriteBlockFilter.WRITE_BLOCKED_MESSAGE,
                body.get("message").asText());
    }

    @Test
    public void shouldAllowOperationsBackupQuery() throws ServletException, IOException {
        RestoreWriteBlockFilter filter = filter(blockedState(), new RestoreWriteBlockProperties());
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain chain = new CountingFilterChain();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/operations/backup/page"), response, chain);

        assertTrue(chain.invoked);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void shouldAllowRestoreExecute() throws ServletException, IOException {
        RestoreWriteBlockFilter filter = filter(blockedState(), new RestoreWriteBlockProperties());
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain chain = new CountingFilterChain();

        filter.doFilter(new MockHttpServletRequest("POST", "/api/operations/restore/execute"), response, chain);

        assertTrue(chain.invoked);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void shouldAllowWriteRequestWhenDisabled() throws ServletException, IOException {
        RestoreWriteBlockProperties properties = new RestoreWriteBlockProperties();
        properties.setEnabled(false);
        RestoreWriteBlockFilter filter = filter(blockedState(), properties);
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain chain = new CountingFilterChain();

        filter.doFilter(new MockHttpServletRequest("POST", "/api/classics/sancai/save"), response, chain);

        assertTrue(chain.invoked);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void shouldTreatNullAllowedPathsAsEmptyList() throws ServletException, IOException {
        RestoreWriteBlockProperties properties = new RestoreWriteBlockProperties();
        properties.setAllowedPaths(null);
        RestoreWriteBlockFilter filter = filter(blockedState(), properties);
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain chain = new CountingFilterChain();

        filter.doFilter(new MockHttpServletRequest("POST", "/api/operations/restore/execute"), response, chain);

        assertFalse(chain.invoked);
        assertEquals(HttpStatus.LOCKED.value(), response.getStatus());
    }

    @Test
    public void shouldExposeAllowedPathsAsImmutableListWithoutNullElements() {
        RestoreWriteBlockProperties properties = new RestoreWriteBlockProperties();
        properties.setAllowedPaths(java.util.Arrays.asList("/api/auth/captcha", null));

        assertEquals(List.of("/api/auth/captcha"), properties.getAllowedPaths());
        assertThrows(
                UnsupportedOperationException.class,
                () -> properties.getAllowedPaths().clear());
    }

    private RestoreWriteBlockFilter filter(RestoreWriteBlockState state, RestoreWriteBlockProperties properties) {
        return new RestoreWriteBlockFilter(state, properties, OBJECT_MAPPER);
    }

    private RestoreWriteBlockState blockedState() {
        RestoreWriteBlockState state = new RestoreWriteBlockState();
        state.enable("restore-1");
        return state;
    }

    private static final class CountingFilterChain implements jakarta.servlet.FilterChain {

        private boolean invoked;

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
            invoked = true;
        }
    }
}
