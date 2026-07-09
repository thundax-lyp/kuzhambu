package com.thundax.kuzhambu.discovery.infra.search.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.Test;

class SearchMapperParameterTest {

    @Test
    void searchLogRangeMapperShouldDeclareParamNames() throws NoSuchMethodException {
        Method method = SearchLogMapper.class.getMethod("selectByCreatedAtRange", Date.class, Date.class);

        assertEquals(List.of("createdAtStart", "createdAtEnd"), paramNames(method));
    }

    @Test
    void searchClickRangeMapperShouldDeclareParamNames() throws NoSuchMethodException {
        Method method = SearchClickMapper.class.getMethod("countByCreatedAtRange", Date.class, Date.class);

        assertEquals(List.of("createdAtStart", "createdAtEnd"), paramNames(method));
    }

    private List<String> paramNames(Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(Param.class))
                .map(Param::value)
                .toList();
    }
}
