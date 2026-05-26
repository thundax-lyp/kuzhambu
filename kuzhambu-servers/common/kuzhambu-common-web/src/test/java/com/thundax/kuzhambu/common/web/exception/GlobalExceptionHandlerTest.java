package com.thundax.kuzhambu.common.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.common.web.i18n.I18nMessageResolver;
import com.thundax.kuzhambu.common.web.response.ApiResponse;
import java.util.Collections;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

public class GlobalExceptionHandlerTest {

    private final StaticMessageSource messageSource = new StaticMessageSource();
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(
            new I18nMessageResolver(messageSource), Collections.singletonList(new DefaultExceptionTranslator()));

    @Test
    public void shouldConvertKuzhambuExceptionToFailureResponse() {
        messageSource.addMessage(WebErrorCode.BAD_REQUEST.getMessageKey(), Locale.getDefault(), "Bad request");

        ResponseEntity<ApiResponse<Object>> response =
                handler.handleKuzhambuException(new BadRequestException("参数缺失"), null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(WebErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("Bad request", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    public void shouldConvertBindExceptionToBadRequestResponse() {
        BindException exception = new BindException(new Object(), "request");
        exception.addError(new FieldError("request", "name", "名称不能为空"));

        ResponseEntity<ApiResponse<Object>> response = handler.handleException(exception, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(WebErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("名称不能为空", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    public void shouldConvertAccessDeniedExceptionToForbiddenResponse() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleException(new AccessDeniedException("denied"), null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(WebErrorCode.FORBIDDEN.getCode(), response.getBody().getCode());
        assertEquals(WebErrorCode.FORBIDDEN.getMessage(), response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    public void shouldConvertUnknownExceptionToSystemFailureResponse() {
        ResponseEntity<ApiResponse<Object>> response = handler.handleException(new RuntimeException("boom"), null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(WebErrorCode.SYSTEM_ERROR.getCode(), response.getBody().getCode());
        assertEquals(WebErrorCode.SYSTEM_ERROR.getMessage(), response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}
