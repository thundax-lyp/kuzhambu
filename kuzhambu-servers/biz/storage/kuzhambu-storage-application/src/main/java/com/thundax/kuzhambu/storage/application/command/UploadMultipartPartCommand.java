package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartNumber;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import java.io.InputStream;

public record UploadMultipartPartCommand(
        MultipartUploadId uploadId,
        MultipartPartNumber partNumber,
        String etag,
        StorageByteSize size,
        InputStream inputStream) {}
