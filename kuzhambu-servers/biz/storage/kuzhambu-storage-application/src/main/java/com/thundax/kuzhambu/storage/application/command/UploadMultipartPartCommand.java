package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartNumber;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadMultipartPartCommand {
    private MultipartUploadId uploadId;
    private MultipartPartNumber partNumber;
    private String etag;
    private StorageByteSize size;
    private InputStream inputStream;
}
