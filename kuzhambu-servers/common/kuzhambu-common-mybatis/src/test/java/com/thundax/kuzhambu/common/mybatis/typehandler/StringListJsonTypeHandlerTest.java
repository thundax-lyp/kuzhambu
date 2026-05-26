package com.thundax.kuzhambu.common.mybatis.typehandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

public class StringListJsonTypeHandlerTest {

    private final StringListJsonTypeHandler typeHandler = new StringListJsonTypeHandler();

    @Test
    public void shouldWriteStringListAsJson() throws Exception {
        PreparedStatement preparedStatement = JdbcStatementStub.preparedStatement();

        typeHandler.setNonNullParameter(preparedStatement, 1, Arrays.asList("admin", "user"), JdbcType.VARCHAR);

        assertEquals(
                "[\"admin\",\"user\"]",
                JdbcStatementStub.from(preparedStatement).written("setString", 1));
    }

    @Test
    public void shouldWriteEmptyListAsJsonArray() throws Exception {
        PreparedStatement preparedStatement = JdbcStatementStub.preparedStatement();

        typeHandler.setNonNullParameter(preparedStatement, 1, Collections.emptyList(), JdbcType.VARCHAR);

        assertEquals("[]", JdbcStatementStub.from(preparedStatement).written("setString", 1));
    }

    @Test
    public void shouldReadStringListByColumnName() throws Exception {
        ResultSet resultSet = JdbcStatementStub.resultSet();
        JdbcStatementStub.from(resultSet).withValue("roles", "[\"admin\",\"user\"]");

        assertEquals(Arrays.asList("admin", "user"), typeHandler.getNullableResult(resultSet, "roles"));
    }

    @Test
    public void shouldReadStringListByColumnIndex() throws Exception {
        ResultSet resultSet = JdbcStatementStub.resultSet();
        JdbcStatementStub.from(resultSet).withValue(1, "[\"admin\"]");

        assertEquals(Collections.singletonList("admin"), typeHandler.getNullableResult(resultSet, 1));
    }

    @Test
    public void shouldReadStringListFromCallableStatement() throws Exception {
        CallableStatement callableStatement = JdbcStatementStub.callableStatement();
        JdbcStatementStub.from(callableStatement).withValue(1, "[\"admin\"]");

        assertEquals(Collections.singletonList("admin"), typeHandler.getNullableResult(callableStatement, 1));
    }

    @Test
    public void shouldReadEmptyDatabaseValueAsEmptyList() throws Exception {
        ResultSet resultSet = JdbcStatementStub.resultSet();
        JdbcStatementStub.from(resultSet).withValue("roles", " ");

        assertEquals(Collections.emptyList(), typeHandler.getNullableResult(resultSet, "roles"));
    }
}
