package com.thundax.kuzhambu.common.web.advice;

import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiResponse;
import com.thundax.kuzhambu.common.web.response.ApiResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final MediaType APPLICATION_JSON_UTF8 =
            new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (isJsonContent(selectedContentType) && response != null) {
            response.getHeaders().setContentType(APPLICATION_JSON_UTF8);
        }

        if (!shouldWrap(returnType) || body instanceof ApiResponse || body instanceof String) {
            return body;
        }
        return ApiResponse.success(body);
    }

    private boolean shouldWrap(MethodParameter returnType) {
        Class<?> containingClass = returnType.getContainingClass();
        boolean wrappedController =
                containingClass != null && containingClass.isAnnotationPresent(WrappedApiController.class);
        if (!wrappedController && !returnType.hasMethodAnnotation(WrappedApiResponse.class)) {
            return false;
        }

        Class<?> parameterType = returnType.getParameterType();
        return !ApiResponse.class.isAssignableFrom(parameterType) && !String.class.isAssignableFrom(parameterType);
    }

    private boolean isJsonContent(MediaType selectedContentType) {
        if (selectedContentType == null) {
            return false;
        }
        return MediaType.APPLICATION_JSON.includes(selectedContentType)
                || (selectedContentType.getSubtype() != null
                        && selectedContentType.getSubtype().endsWith("+json"));
    }
}
