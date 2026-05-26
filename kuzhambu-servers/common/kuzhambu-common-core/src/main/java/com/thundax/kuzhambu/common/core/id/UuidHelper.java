package com.thundax.kuzhambu.common.core.id;

import java.util.UUID;

/**
 * 封装各种生成唯一性ID算法的工具类.
 */
public final class UuidHelper {

    private UuidHelper() {}

    public static String compact() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
