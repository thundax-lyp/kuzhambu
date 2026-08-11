package com.thundax.kuzhambu.ai.domain.invocation.repository;

import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;

public final class AiInvocationContentRefMatcher {

    private AiInvocationContentRefMatcher() {
        // Utility class
    }

    public static boolean matches(AiContentRef actual, AiContentRef expected) {
        String expectedType = AiContentRefCodec.toContentType(expected);
        Long expectedId = AiContentRefCodec.toContentId(expected);
        if (expectedType == null && expectedId == null) {
            return true;
        }
        if (actual == null) {
            return false;
        }
        String actualType = AiContentRefCodec.toContentType(actual);
        Long actualId = AiContentRefCodec.toContentId(actual);
        return (expectedType == null || expectedType.equals(actualType))
                && (expectedId == null || expectedId.equals(actualId));
    }
}
