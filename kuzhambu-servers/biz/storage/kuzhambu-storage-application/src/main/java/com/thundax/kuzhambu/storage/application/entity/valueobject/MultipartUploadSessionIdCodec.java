package com.thundax.kuzhambu.storage.application.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class MultipartUploadSessionIdCodec {

    private MultipartUploadSessionIdCodec() {}

    public static MultipartUploadSessionId toDomain(Long value) {
        return MultipartUploadSessionId.ofNullable(value);
    }

    public static Long toValue(MultipartUploadSessionId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(MultipartUploadSessionId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<MultipartUploadSessionId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(MultipartUploadSessionIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<MultipartUploadSessionId> ids) {
        return ids == null
                ? null
                : ids.stream().map(MultipartUploadSessionIdCodec::toValue).collect(Collectors.toList());
    }
}
