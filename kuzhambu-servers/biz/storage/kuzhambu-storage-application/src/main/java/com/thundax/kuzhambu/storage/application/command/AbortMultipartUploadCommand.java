package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;

public record AbortMultipartUploadCommand(MultipartUploadId uploadId) {}
