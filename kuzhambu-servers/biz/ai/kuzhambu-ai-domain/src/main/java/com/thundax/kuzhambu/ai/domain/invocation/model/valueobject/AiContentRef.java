package com.thundax.kuzhambu.ai.domain.invocation.model.valueobject;

import java.util.Objects;

public final class AiContentRef {

    private final String contentType;
    private final Long contentId;

    private AiContentRef(String contentType, Long contentId) {
        this.contentType = contentType;
        this.contentId = contentId;
    }

    public static AiContentRef of(String contentType, Long contentId) {
        return new AiContentRef(contentType, contentId);
    }

    public static AiContentRef ofNullable(String contentType, Long contentId) {
        if ((contentType == null || contentType.trim().isEmpty()) && contentId == null) {
            return null;
        }
        return new AiContentRef(contentType, contentId);
    }

    public String contentType() {
        return contentType;
    }

    public Long contentId() {
        return contentId;
    }

    public AiContentRef withContentType(String value) {
        return ofNullable(value, contentId);
    }

    public AiContentRef withContentId(Long value) {
        return ofNullable(contentType, value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AiContentRef that = (AiContentRef) other;
        return Objects.equals(contentType, that.contentType) && Objects.equals(contentId, that.contentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentType, contentId);
    }
}
