package com.thundax.kuzhambu.common.mybatis.typehandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

public class InstantEpochMillisTypeHandlerTest {

    private final InstantEpochMillisTypeHandler typeHandler = new InstantEpochMillisTypeHandler();

    @Test
    public void shouldWriteInstantAsEpochMilliseconds() throws Exception {
        PreparedStatement preparedStatement = JdbcStatementStub.preparedStatement();

        typeHandler.setNonNullParameter(
                preparedStatement, 1, Instant.parse("2026-02-26T20:00:00.123456Z"), JdbcType.BIGINT);

        assertEquals(1772136000123L, JdbcStatementStub.from(preparedStatement).written("setLong", 1));
    }

    @Test
    public void shouldWritePreEpochInstantAsNegativeEpochMilliseconds() throws Exception {
        PreparedStatement preparedStatement = JdbcStatementStub.preparedStatement();

        typeHandler.setNonNullParameter(preparedStatement, 1, Instant.parse("1569-12-31T16:00:00Z"), JdbcType.BIGINT);

        assertEquals(-12622809600000L, JdbcStatementStub.from(preparedStatement).written("setLong", 1));
    }

    @Test
    public void shouldReadInstantByColumnName() throws Exception {
        ResultSet resultSet = JdbcStatementStub.resultSet();
        JdbcStatementStub.from(resultSet)
                .withValue("created_at", 1772136000000L)
                .withWasNull(false);

        assertEquals(Instant.parse("2026-02-26T20:00:00Z"), typeHandler.getNullableResult(resultSet, "created_at"));
    }

    @Test
    public void shouldReadInstantByColumnIndex() throws Exception {
        ResultSet resultSet = JdbcStatementStub.resultSet();
        JdbcStatementStub.from(resultSet).withValue(1, -12622809600000L).withWasNull(false);

        assertEquals(Instant.parse("1569-12-31T16:00:00Z"), typeHandler.getNullableResult(resultSet, 1));
    }

    @Test
    public void shouldReadInstantFromCallableStatement() throws Exception {
        CallableStatement callableStatement = JdbcStatementStub.callableStatement();
        JdbcStatementStub.from(callableStatement).withValue(1, 1772136000000L).withWasNull(false);

        assertEquals(Instant.parse("2026-02-26T20:00:00Z"), typeHandler.getNullableResult(callableStatement, 1));
    }

    @Test
    public void shouldReadSqlNullValueAsNull() throws Exception {
        ResultSet resultSet = JdbcStatementStub.resultSet();
        JdbcStatementStub.from(resultSet).withValue("created_at", 0L).withWasNull(true);

        assertNull(typeHandler.getNullableResult(resultSet, "created_at"));
    }
}
