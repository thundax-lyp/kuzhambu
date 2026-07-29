package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageBucketName;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageObjectKey;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InitMultipartUploadCommand {
    private MultipartUploadId uploadId;
    private StorageOwnerRef ownerRef;
    private String businessType;
    private String originalFilename;
    private StorageMimeType mimeType;
    private StorageBucketName bucketName;
    private StorageObjectKey objectKey;
    private MultipartUploadId providerUploadId;
    private StorageByteSize totalSize;
    private MultipartPartSize partSize;
}
