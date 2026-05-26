package com.thundax.kuzhambu.common.web.exception;

import com.thundax.kuzhambu.common.web.i18n.I18nMessageResolver;
import com.thundax.kuzhambu.common.web.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final I18nMessageResolver i18nMessageResolver;
    private final List<ExceptionTranslator> exceptionTranslators;

    public GlobalExceptionHandler(
            I18nMessageResolver i18nMessageResolver, List<ExceptionTranslator> exceptionTranslators) {
        this.i18nMessageResolver = i18nMessageResolver;
        this.exceptionTranslators = sortedTranslators(exceptionTranslators);
    }

    @ExceptionHandler(KuzhambuException.class)
    public ResponseEntity<ApiResponse<Object>> handleKuzhambuException(
            KuzhambuException exception, HttpServletRequest request) {
        if (isSystemError(exception)) {
            logException(request, exception);
        }
        return toResponseEntity(exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception exception, HttpServletRequest request) {
        KuzhambuException translatedException = translate(exception);
        if (translatedException != null) {
            if (isSystemError(translatedException)) {
                logException(request, exception);
            }
            return toResponseEntity(translatedException);
        }
        logException(request, exception);
        return toResponseEntity(new SystemException());
    }

    private boolean isSystemError(KuzhambuException exception) {
        return exception != null && exception.getHttpStatus() >= 500;
    }

    private void logException(HttpServletRequest request, Exception exception) {
        if (request == null) {
            LOGGER.error("Unhandled API exception", exception);
            return;
        }
        LOGGER.error(
                "Unhandled API exception: method={}, uri={}, requestId={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getHeader("X-Request-Id"),
                exception);
    }

    private KuzhambuException translate(Exception exception) {
        for (ExceptionTranslator translator : exceptionTranslators) {
            KuzhambuException translatedException = translator.translate(exception);
            if (translatedException != null) {
                return translatedException;
            }
        }
        return null;
    }

    private ResponseEntity<ApiResponse<Object>> toResponseEntity(KuzhambuException exception) {
        return ResponseEntity.status(exception.getHttpStatus())
                .body(ApiResponse.failure(
                        exception.getCode(),
                        i18nMessageResolver.resolve(
                                exception.getMessageKey(), exception.getDefaultMessage(), exception.getMessageArgs())));
    }

    private List<ExceptionTranslator> sortedTranslators(List<ExceptionTranslator> translators) {
        if (translators == null || translators.isEmpty()) {
            return Collections.emptyList();
        }
        List<ExceptionTranslator> sortedTranslators = new ArrayList<>(translators);
        AnnotationAwareOrderComparator.sort(sortedTranslators);
        return sortedTranslators;
    }
}
