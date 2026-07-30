package com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

class QaSessionMapperTest {

    @Test
    void selectByOpenedAtRangeShouldKeepHistoricalSessions() throws NoSuchMethodException {
        Method method = selectByOpenedAtRangeMethod();

        String sql = String.join("\n", method.getAnnotation(Select.class).value());

        assertFalse(sql.contains("removed_at is null"));
        assertTrue(sql.contains("opened_at >="));
        assertTrue(sql.contains("opened_at <="));
    }

    @Test
    void selectByOpenedAtRangeShouldDeclareParamNames() throws NoSuchMethodException {
        Method method = selectByOpenedAtRangeMethod();

        assertEquals(List.of("openedAtStart", "openedAtEnd"), paramNames(method));
    }

    private Method selectByOpenedAtRangeMethod() throws NoSuchMethodException {
        return QaSessionMapper.class.getMethod("selectByOpenedAtRange", Instant.class, Instant.class);
    }

    private List<String> paramNames(Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(Param.class))
                .map(Param::value)
                .toList();
    }
}
