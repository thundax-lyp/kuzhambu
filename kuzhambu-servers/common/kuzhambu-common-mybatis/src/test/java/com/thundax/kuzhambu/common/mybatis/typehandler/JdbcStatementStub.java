package com.thundax.kuzhambu.common.mybatis.typehandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

final class JdbcStatementStub implements InvocationHandler {

    private final Map<Object, Object> values = new HashMap<Object, Object>();
    private final Map<String, Object> writes = new HashMap<String, Object>();
    private boolean wasNull;

    private JdbcStatementStub() {}

    static PreparedStatement preparedStatement() {
        return proxy(PreparedStatement.class, new JdbcStatementStub());
    }

    static ResultSet resultSet() {
        return proxy(ResultSet.class, new JdbcStatementStub());
    }

    static CallableStatement callableStatement() {
        return proxy(CallableStatement.class, new JdbcStatementStub());
    }

    static JdbcStatementStub from(Object proxy) {
        return (JdbcStatementStub) Proxy.getInvocationHandler(proxy);
    }

    JdbcStatementStub withValue(Object key, Object value) {
        values.put(key, value);
        return this;
    }

    JdbcStatementStub withWasNull(boolean wasNull) {
        this.wasNull = wasNull;
        return this;
    }

    Object written(String method, int index) {
        return writes.get(writeKey(method, index));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        if ("setString".equals(methodName) || "setLong".equals(methodName)) {
            writes.put(writeKey(methodName, (Integer) args[0]), args[1]);
            return null;
        }
        if ("getString".equals(methodName) || "getLong".equals(methodName)) {
            Object value = values.get(args[0]);
            return value == null ? defaultValue(method.getReturnType()) : value;
        }
        if ("wasNull".equals(methodName)) {
            return wasNull;
        }
        if ("toString".equals(methodName)) {
            return "JdbcStatementStub";
        }
        return defaultValue(method.getReturnType());
    }

    private static String writeKey(String method, int index) {
        return method + ":" + index;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, JdbcStatementStub handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (Boolean.TYPE.equals(type)) {
            return false;
        }
        if (Byte.TYPE.equals(type)) {
            return (byte) 0;
        }
        if (Short.TYPE.equals(type)) {
            return (short) 0;
        }
        if (Integer.TYPE.equals(type)) {
            return 0;
        }
        if (Long.TYPE.equals(type)) {
            return 0L;
        }
        if (Float.TYPE.equals(type)) {
            return 0F;
        }
        if (Double.TYPE.equals(type)) {
            return 0D;
        }
        if (Character.TYPE.equals(type)) {
            return '\0';
        }
        return null;
    }
}
