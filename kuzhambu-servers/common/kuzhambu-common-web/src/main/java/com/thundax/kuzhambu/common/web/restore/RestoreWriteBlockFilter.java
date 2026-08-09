package com.thundax.kuzhambu.common.web.restore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.web.configure.RestoreWriteBlockProperties;
import com.thundax.kuzhambu.common.web.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class RestoreWriteBlockFilter extends OncePerRequestFilter {

    public static final String WRITE_BLOCKED_CODE = "OPERATIONS-RESTORE-WRITE-BLOCKED";
    public static final String WRITE_BLOCKED_MESSAGE = "系统正在执行恢复，请稍后重试。";

    private final RestoreWriteBlockState state;
    private final RestoreWriteBlockProperties properties;
    private final ObjectMapper objectMapper;

    public RestoreWriteBlockFilter(
            RestoreWriteBlockState state, RestoreWriteBlockProperties properties, ObjectMapper objectMapper) {
        this.state = state;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!shouldBlock(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.LOCKED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(WRITE_BLOCKED_CODE, WRITE_BLOCKED_MESSAGE));
    }

    private boolean shouldBlock(HttpServletRequest request) {
        return properties.isEnabled()
                && state.isBlocked()
                && isWriteMethod(request.getMethod())
                && !isAllowedPath(request.getRequestURI());
    }

    private boolean isWriteMethod(String method) {
        String normalizedMethod = method == null ? "" : method.toUpperCase(Locale.ROOT);
        return "POST".equals(normalizedMethod)
                || "PUT".equals(normalizedMethod)
                || "PATCH".equals(normalizedMethod)
                || "DELETE".equals(normalizedMethod);
    }

    private boolean isAllowedPath(String requestUri) {
        if (requestUri == null) {
            return false;
        }
        return properties.getAllowedPaths().stream().anyMatch(requestUri::equals);
    }
}
