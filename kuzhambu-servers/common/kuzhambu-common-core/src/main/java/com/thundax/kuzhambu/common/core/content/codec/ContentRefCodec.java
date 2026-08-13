package com.thundax.kuzhambu.common.core.content.codec;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;

public final class ContentRefCodec {

    private ContentRefCodec() {}

    public static ContentRef toDomain(String contentType, Long contentId) {
        return contentType == null && contentId == null ? null : new ContentRef(contentType, contentId);
    }

    public static Long toValue(ContentRef contentRef) {
        return contentRef == null ? null : contentRef.getContentId();
    }
}
