package com.thundax.kuzhambu.common.mybatis.typehandler;

import com.thundax.kuzhambu.common.core.crypto.Sm4Crypto;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes({String.class})
public class DefaultEncryptTypeHandler extends BaseTypeHandler<String> {

    private static final String SALT = "PJ-1712PJ-1712";
    private static final String PREFIX = "ENC(WDIT:";

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int index, String value, JdbcType jdbcType)
            throws SQLException {
        preparedStatement.setString(index, encrypt(value));
    }

    @Override
    public String getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return decrypt(resultSet.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return decrypt(resultSet.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return decrypt(callableStatement.getString(columnIndex));
    }

    public static String encrypt(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        String encryptedValue = Sm4Crypto.encryptEcb(SALT, value);
        return encryptedValue == null ? value : PREFIX + encryptedValue;
    }

    public static String decrypt(String value) {
        if (!StringUtils.startsWith(value, PREFIX)) {
            return value;
        }
        String decryptValue = Sm4Crypto.decryptEcb(SALT, StringUtils.substring(value, PREFIX.length()));
        return decryptValue == null ? value : decryptValue;
    }
}
