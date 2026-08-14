package com.thundax.kuzhambu.common.core.content.valueobject;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class ContentRef {
    private final String contentType;
    private final Long contentId;

    public ContentRef(String contentType, Long contentId) {
        if (contentType == null || contentType.isBlank()) {
            throw new DomainException("内容类型不能为空");
        }
        if (contentId == null || contentId <= 0) {
            throw new DomainException("内容标识必须为正整数");
        }
        this.contentType = contentType;
        this.contentId = contentId;
    }
}
