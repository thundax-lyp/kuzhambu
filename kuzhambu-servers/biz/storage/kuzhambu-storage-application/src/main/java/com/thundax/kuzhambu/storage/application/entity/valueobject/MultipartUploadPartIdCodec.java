package com.thundax.kuzhambu.storage.application.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class MultipartUploadPartIdCodec {

    private MultipartUploadPartIdCodec() {}

    public static MultipartUploadPartId toDomain(Long value) {
        return MultipartUploadPartId.ofNullable(value);
    }

    public static Long toValue(MultipartUploadPartId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(MultipartUploadPartId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<MultipartUploadPartId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(MultipartUploadPartIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<MultipartUploadPartId> ids) {
        return ids == null
                ? null
                : ids.stream().map(MultipartUploadPartIdCodec::toValue).collect(Collectors.toList());
    }
}
