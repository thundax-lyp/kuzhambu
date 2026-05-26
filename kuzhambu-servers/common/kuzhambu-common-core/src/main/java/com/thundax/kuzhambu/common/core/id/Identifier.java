package com.thundax.kuzhambu.common.core.id;

public interface Identifier<T> {

    T value();

    Class<T> type();

    default String asString() {
        return String.valueOf(value());
    }
}
