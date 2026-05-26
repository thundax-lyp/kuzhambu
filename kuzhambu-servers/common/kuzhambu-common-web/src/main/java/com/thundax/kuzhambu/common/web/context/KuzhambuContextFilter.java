package com.thundax.kuzhambu.common.web.context;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class KuzhambuContextFilter extends OncePerRequestFilter {

    public static final String MDC_REQUEST_ID = "requestId";

    private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("com.thundax.kuzhambu.access");
    private static final long DEFAULT_SLOW_REQUEST_MILLIS = 1000L;

    private final KuzhambuContextResolver resolver;
    private final long slowRequestMillis;

    public KuzhambuContextFilter(KuzhambuContextResolver resolver) {
        this(resolver, DEFAULT_SLOW_REQUEST_MILLIS);
    }

    public KuzhambuContextFilter(KuzhambuContextResolver resolver, long slowRequestMillis) {
        this.resolver = resolver;
        this.slowRequestMillis = slowRequestMillis;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startNanos = System.nanoTime();
        String requestId = resolver.resolveRequestId(request);
        KuzhambuContextHolder.setRequestId(requestId);
        if (requestId != null) {
            MDC.put(MDC_REQUEST_ID, requestId);
            response.setHeader(DefaultKuzhambuContextResolver.HEADER_REQUEST_ID, requestId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            logAccess(request, response, requestId, elapsedMillis(startNanos));
            MDC.remove(MDC_REQUEST_ID);
            KuzhambuContextHolder.clearRequestContext();
        }
    }

    private void logAccess(
            HttpServletRequest request, HttpServletResponse response, String requestId, long elapsedMillis) {
        KuzhambuSubject subject = KuzhambuContextHolder.currentSubject();
        String subjectId = subject != null ? subject.getSubjectId() : null;
        String subjectType = subject != null && subject.getSubjectType() != null
                ? subject.getSubjectType().name()
                : null;
        if (elapsedMillis >= slowRequestMillis) {
            ACCESS_LOGGER.warn(
                    "slow request method={} uri={} status={} elapsedMs={} requestId={} subjectType={} subjectId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    elapsedMillis,
                    requestId,
                    subjectType,
                    subjectId);
            return;
        }
        ACCESS_LOGGER.info(
                "request method={} uri={} status={} elapsedMs={} requestId={} subjectType={} subjectId={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                elapsedMillis,
                requestId,
                subjectType,
                subjectId);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1000000L;
    }
}
