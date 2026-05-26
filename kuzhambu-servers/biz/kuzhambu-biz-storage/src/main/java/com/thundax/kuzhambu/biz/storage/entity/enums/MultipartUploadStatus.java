package com.thundax.kuzhambu.biz.storage.entity.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum MultipartUploadStatus {
    INITIATED,
    UPLOADING,
    COMPLETED,
    ABORTED;

    public String value() {
        return name();
    }

    public static MultipartUploadStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "STORAGE-90001",
                        "storage.domain.multipart-upload-status.invalid",
                        "Unknown multipart upload status: " + value));
    }
}
