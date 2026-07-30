package com.thundax.kuzhambu.system.interfaces.admin.core.aop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.util.RequestIpUtils;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.domain.core.model.enums.LogType;
import com.thundax.kuzhambu.system.interfaces.admin.core.service.SysLogMessageService;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SysLogMethodInterceptor implements MethodInterceptor {

    private static final String TITLE_SEPARATOR = "-";
    private static final String MASKED_VALUE = "******";
    private static final Set<String> SENSITIVE_FIELD_NAMES = Set.of(
            "token",
            "accessToken",
            "refreshToken",
            "loginToken",
            "password",
            "loginPass",
            "oldPassword",
            "newPassword",
            "privateKey",
            "secret",
            "credential");

    private final SysLogMessageService sysLogMessageService;
    private final ObjectMapper objectMapper;

    public SysLogMethodInterceptor(SysLogMessageService sysLogMessageService, ObjectMapper objectMapper) {
        this.sysLogMessageService = sysLogMessageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        String[] modules = null;
        String value = null;
        String category = null;

        Method method = methodInvocation.getMethod();

        SysLogger annotation = AnnotationUtils.findAnnotation(method, SysLogger.class);
        if (annotation != null) {
            modules = annotation.module();
            value = annotation.value();
            category = annotation.category();
        }

        if (StringUtils.isEmpty(value) || ArrayUtils.isEmpty(modules) || StringUtils.isEmpty(category)) {
            SysLogger parentAnnotation = AnnotationUtils.findAnnotation(method.getDeclaringClass(), SysLogger.class);
            Assert.notNull(parentAnnotation, "modules of annotation '@SysLogger' is empty");

            if (StringUtils.isEmpty(value)) {
                value = parentAnnotation.value();
                Assert.notNull(value, "value of annotation '@SysLogger' is empty");
            }

            if (StringUtils.isEmpty(category)) {
                category = parentAnnotation.category();
            }

            if (ArrayUtils.isEmpty(modules)) {
                modules = parentAnnotation.module();
                Assert.notEmpty(modules, "modules of annotation '@SysLogger' is empty");
            }
        }

        Object requestBody = findRequestObjectArgument(method, methodInvocation.getArguments());

        writeLog(modules, value, category, requestBody);

        return methodInvocation.proceed();
    }

    private Object findRequestObjectArgument(Method method, Object[] arguments) {
        if (ArrayUtils.isEmpty(arguments)) {
            return null;
        }

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int parameterIdx = 0; parameterIdx < parameterAnnotations.length; parameterIdx++) {
            Annotation[] annotations = parameterAnnotations[parameterIdx];
            if (ArrayUtils.isEmpty(annotations)) {
                continue;
            }

            for (Annotation annotation : annotations) {
                if (annotation instanceof RequestBody) {
                    return arguments[parameterIdx];
                }
            }
        }

        return null;
    }

    private void writeLog(String[] modules, String value, String category, Object requestBody) {
        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        List<String> titleParts = new ArrayList<>(Arrays.asList(modules));
        titleParts.add(value);

        Log log = new Log();
        log.setUserId(UserIdCodec.toDomain(KuzhambuContextHolder.currentSubjectId()));
        log.setTitle(StringUtils.join(titleParts, TITLE_SEPARATOR));

        log.setLogDate(Instant.now());

        log.setRemoteAddr(RequestIpUtils.getIpAddr(currentRequest));
        log.setUserAgent(currentRequest.getHeader("user-agent"));
        log.setRequestUri(currentRequest.getRequestURI());
        log.setMethod(currentRequest.getMethod());

        if (StringUtils.isEmpty(category)) {
            log.setType(LogType.ACCESS);
        } else {
            log.setType(LogType.from(category));
        }

        if (requestBody != null) {
            log.setRequestParams(toJson(requestBody));
        }

        sysLogMessageService.saveLog(log);
    }

    private String toJson(Object requestBody) {
        try {
            return objectMapper.writeValueAsString(maskSensitiveFields(objectMapper.valueToTree(requestBody)));
        } catch (Exception e) {
            return null;
        }
    }

    private Object maskSensitiveFields(Object value) {
        if (value instanceof ObjectNode objectNode) {
            Set<String> fieldNames = new HashSet<>();
            objectNode.fieldNames().forEachRemaining(fieldNames::add);
            for (String fieldName : fieldNames) {
                if (isSensitiveField(fieldName)) {
                    objectNode.put(fieldName, MASKED_VALUE);
                } else {
                    maskSensitiveFields(objectNode.get(fieldName));
                }
            }
            return objectNode;
        }
        if (value instanceof ArrayNode arrayNode) {
            for (JsonNode item : arrayNode) {
                maskSensitiveFields(item);
            }
        }
        return value;
    }

    private boolean isSensitiveField(String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            return false;
        }
        String normalized = fieldName.toLowerCase(Locale.ROOT);
        for (String sensitiveFieldName : SENSITIVE_FIELD_NAMES) {
            if (normalized.equals(sensitiveFieldName.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return normalized.endsWith("token") || normalized.endsWith("password") || normalized.endsWith("secret");
    }
}
