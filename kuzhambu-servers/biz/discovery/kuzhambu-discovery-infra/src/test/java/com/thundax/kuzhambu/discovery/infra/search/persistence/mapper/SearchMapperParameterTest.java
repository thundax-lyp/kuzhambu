package com.thundax.kuzhambu.discovery.infra.search.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.Test;

class SearchMapperParameterTest {

    @Test
    void searchEventRangeMapperShouldDeclareParamNames() throws NoSuchMethodException {
        Method method = SearchEventMapper.class.getMethod("selectByCreatedAtRange", Instant.class, Instant.class);

        assertEquals(List.of("createdAtStart", "createdAtEnd"), paramNames(method));
    }

    @Test
    void searchClickEventRangeMapperShouldDeclareParamNames() throws NoSuchMethodException {
        Method method = SearchClickEventMapper.class.getMethod("countByCreatedAtRange", Instant.class, Instant.class);

        assertEquals(List.of("createdAtStart", "createdAtEnd"), paramNames(method));
    }

    private List<String> paramNames(Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(Param.class))
                .map(Param::value)
                .toList();
    }
}
