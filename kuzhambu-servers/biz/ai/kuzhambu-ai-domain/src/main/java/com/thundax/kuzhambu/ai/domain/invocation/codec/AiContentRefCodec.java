package com.thundax.kuzhambu.ai.domain.invocation.codec;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;

public final class AiContentRefCodec {

    private AiContentRefCodec() {}

    public static AiContentRef toDomain(String contentType, Long contentId) {
        return AiContentRef.ofNullable(contentType, contentId);
    }

    public static String toContentType(AiContentRef contentRef) {
        return contentRef == null ? null : contentRef.contentType();
    }

    public static Long toContentId(AiContentRef contentRef) {
        return contentRef == null ? null : contentRef.contentId();
    }
}
