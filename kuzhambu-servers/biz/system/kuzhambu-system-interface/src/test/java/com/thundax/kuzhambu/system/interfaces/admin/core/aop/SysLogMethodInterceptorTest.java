package com.thundax.kuzhambu.system.interfaces.admin.core.aop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.interfaces.admin.core.service.SysLogMessageService;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class SysLogMethodInterceptorTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldMaskSensitiveRequestBodyFields() throws Throwable {
        CapturingSysLogMessageService messageService = new CapturingSysLogMessageService();
        SysLogMethodInterceptor interceptor = new SysLogMethodInterceptor(messageService, OBJECT_MAPPER);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/session/logout");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        interceptor.invoke(new SimpleMethodInvocation(new SensitiveRequest(
                "access-token-value", "plain-password", new NestedRequest("refresh-token-value", "visible-value"))));

        assertNotNull(messageService.log);
        JsonNode requestParams = OBJECT_MAPPER.readTree(messageService.log.getRequestParams());
        assertEquals("******", requestParams.get("token").asText());
        assertEquals("******", requestParams.get("password").asText());
        assertEquals("******", requestParams.get("nested").get("refreshToken").asText());
        assertEquals("visible-value", requestParams.get("nested").get("visible").asText());
        assertFalse(messageService.log.getRequestParams().contains("access-token-value"));
        assertFalse(messageService.log.getRequestParams().contains("plain-password"));
        assertFalse(messageService.log.getRequestParams().contains("refresh-token-value"));
    }

    static final class TestController {

        @SysLogger(module = "系统", value = "测试", category = "ACCESS")
        Object method(@RequestBody SensitiveRequest request) {
            return null;
        }
    }

    private record SensitiveRequest(String token, String password, NestedRequest nested) {}

    private record NestedRequest(String refreshToken, String visible) {}

    private static final class CapturingSysLogMessageService implements SysLogMessageService {

        private Log log;

        @Override
        public void saveLog(Log sysLog) {
            this.log = sysLog;
        }

        @Override
        public void consumeLog(String payload) {}
    }

    private static final class SimpleMethodInvocation implements MethodInvocation {

        private final Object[] arguments;

        private SimpleMethodInvocation(Object request) {
            this.arguments = new Object[] {request};
        }

        @Override
        public Method getMethod() {
            try {
                return TestController.class.getDeclaredMethod("method", SensitiveRequest.class);
            } catch (NoSuchMethodException exception) {
                throw new IllegalStateException(exception);
            }
        }

        @Override
        public Object[] getArguments() {
            return arguments;
        }

        @Override
        public Object proceed() {
            return null;
        }

        @Override
        public Object getThis() {
            return new TestController();
        }

        @Override
        public AccessibleObject getStaticPart() {
            return getMethod();
        }
    }
}
