package com.thundax.kuzhambu.common.mybatis.typehandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.common.core.id.EntityId;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

public class EntityIdTypeHandlerTest {

    private final EntityIdTypeHandler typeHandler = new EntityIdTypeHandler();

    @Test
    public void shouldWriteEntityIdAsLong() throws Exception {
        PreparedStatement preparedStatement = JdbcStatementStub.preparedStatement();

        typeHandler.setNonNullParameter(preparedStatement, 1, EntityId.of(1001L), JdbcType.BIGINT);

        assertEquals(1001L, JdbcStatementStub.from(preparedStatement).written("setLong", 1));
    }

    @Test
    public void shouldReadEntityIdByColumnName() throws Exception {
        ResultSet resultSet = JdbcStatementStub.resultSet();
        JdbcStatementStub.from(resultSet).withValue("id", 1001L).withWasNull(false);

        assertEquals(EntityId.of(1001L), typeHandler.getNullableResult(resultSet, "id"));
    }

    @Test
    public void shouldReadEntityIdByColumnIndex() throws Exception {
        ResultSet resultSet = JdbcStatementStub.resultSet();
        JdbcStatementStub.from(resultSet).withValue(1, 1001L).withWasNull(false);

        assertEquals(EntityId.of(1001L), typeHandler.getNullableResult(resultSet, 1));
    }

    @Test
    public void shouldReadEntityIdFromCallableStatement() throws Exception {
        CallableStatement callableStatement = JdbcStatementStub.callableStatement();
        JdbcStatementStub.from(callableStatement).withValue(1, 1001L).withWasNull(false);

        assertEquals(EntityId.of(1001L), typeHandler.getNullableResult(callableStatement, 1));
    }

    @Test
    public void shouldReadSqlNullValueAsNull() throws Exception {
        ResultSet resultSet = JdbcStatementStub.resultSet();
        JdbcStatementStub.from(resultSet).withValue("id", 0L).withWasNull(true);

        assertNull(typeHandler.getNullableResult(resultSet, "id"));
    }
}
