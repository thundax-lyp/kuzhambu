package com.thundax.kuzhambu.common.mybatis.typehandler;

import com.thundax.kuzhambu.common.core.id.EntityId;
import com.thundax.kuzhambu.common.core.id.EntityIdCodec;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(EntityId.class)
@MappedJdbcTypes(JdbcType.BIGINT)
public class EntityIdTypeHandler extends BaseTypeHandler<EntityId> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, EntityId parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setLong(i, EntityIdCodec.toValue(parameter));
    }

    @Override
    public EntityId getNullableResult(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : EntityIdCodec.toDomain(value);
    }

    @Override
    public EntityId getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        long value = rs.getLong(columnIndex);
        return rs.wasNull() ? null : EntityIdCodec.toDomain(value);
    }

    @Override
    public EntityId getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        long value = cs.getLong(columnIndex);
        return cs.wasNull() ? null : EntityIdCodec.toDomain(value);
    }
}
