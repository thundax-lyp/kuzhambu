package com.thundax.kuzhambu.storage.interfaces.admin.object.assembler;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.application.query.ListStorageObjectsQuery;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.AbortMultipartUploadRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.CompleteMultipartUploadRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.StoragePageRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.AbortMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.CompleteMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.InitMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.StorageObjectResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.UploadMultipartPartResponse;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class StorageInterfaceAssembler {

    private StorageInterfaceAssembler() {}

    @NonNull
    public static ListStorageObjectsQuery toListQuery(@NonNull StoragePageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ListStorageObjectsQuery(
                null,
                mimeTypeFrom(request.getContentType()),
                objectStatusFrom(request.getObjectStatus()),
                referenceStatusFrom(request.getReferenceStatus()),
                StorageOwnerRef.ofNullable(
                        ownerTypeFrom(request.getReferenceOwnerType()), emptyToNull(request.getReferenceOwnerId())),
                emptyToNull(request.getOriginalFilename()),
                emptyToNull(request.getRemarks()),
                sortDirectionFrom(request.getSortDirection()));
    }

    @NonNull
    public static StorageObjectResponse toResponse(@NonNull StoredObject entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return StorageObjectResponse.builder()
                .id(StoredObjectIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .originalFilename(entity.getOriginalFilename())
                .contentType(entity.getContentType())
                .size(entity.getSize())
                .accessEndpoint(entity.getAccessEndpoint())
                .objectStatus(objectStatusValue(entity.getObjectStatus()))
                .referenceStatus(referenceStatusValue(entity.getReferenceStatus()))
                .referenceOwnerType(entity.getReferenceOwnerType())
                .build();
    }

    @NonNull
    public static InitMultipartUploadResponse toResponse(@NonNull MultipartUploadSession session) {
        Objects.requireNonNull(session, "session must not be null");
        return InitMultipartUploadResponse.builder()
                .uploadId(session.getUploadId())
                .providerUploadId(session.getProviderUploadId())
                .businessType(session.getBusinessType())
                .originalFilename(session.getOriginalFilename())
                .mimeType(session.getMimeType())
                .bucketName(session.getBucketName())
                .objectKey(session.getObjectKey())
                .totalSize(session.getTotalSize())
                .partSize(session.getPartSize())
                .uploadedPartCount(session.getUploadedPartCount())
                .uploadStatus(uploadStatusValue(session.getUploadStatus()))
                .build();
    }

    @NonNull
    public static UploadMultipartPartResponse toResponse(@NonNull MultipartUploadPart part) {
        Objects.requireNonNull(part, "part must not be null");
        return UploadMultipartPartResponse.builder()
                .uploadId(part.getUploadId())
                .partNumber(part.getPartNumber())
                .etag(part.getEtag())
                .size(part.getSize())
                .build();
    }

    @NonNull
    public static CompleteMultipartUploadResponse toResponse(
            @NonNull StoredObject storage, @NonNull CompleteMultipartUploadRequest request) {
        Objects.requireNonNull(storage, "storage must not be null");
        Objects.requireNonNull(request, "request must not be null");
        return CompleteMultipartUploadResponse.builder()
                .id(StoredObjectIdCodec.toStringValue(storage.getId()))
                .uploadId(request.getUploadId())
                .originalFilename(storage.getOriginalFilename())
                .mimeType(storage.getMimeType())
                .bucketName(storage.getBucketName())
                .objectKey(storage.getObjectKey())
                .size(storage.getSize())
                .accessEndpoint(storage.getAccessEndpoint())
                .objectStatus(objectStatusValue(storage.getObjectStatus()))
                .referenceStatus(referenceStatusValue(storage.getReferenceStatus()))
                .build();
    }

    @NonNull
    public static AbortMultipartUploadResponse toResponse(@NonNull AbortMultipartUploadRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return AbortMultipartUploadResponse.builder()
                .uploadId(request.getUploadId())
                .uploadStatus(MultipartUploadStatus.ABORTED.value())
                .build();
    }

    private static StoredObjectStatus objectStatusFrom(String value) {
        return StringUtils.isBlank(value) ? null : StoredObjectStatus.from(value);
    }

    private static StoredObjectReferenceStatus referenceStatusFrom(String value) {
        return StringUtils.isBlank(value) ? null : StoredObjectReferenceStatus.from(value);
    }

    private static StorageOwnerType ownerTypeFrom(String value) {
        return StringUtils.isBlank(value) ? null : StorageOwnerType.from(value);
    }

    private static StorageMimeType mimeTypeFrom(String value) {
        return StringUtils.isBlank(value) ? null : new StorageMimeType(value);
    }

    private static SortDirection sortDirectionFrom(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return SortDirection.valueOf(value.trim().toUpperCase());
    }

    private static String emptyToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private static String objectStatusValue(StoredObjectStatus value) {
        return value == null ? null : value.value();
    }

    private static String referenceStatusValue(StoredObjectReferenceStatus value) {
        return value == null ? null : value.value();
    }

    private static String uploadStatusValue(MultipartUploadStatus value) {
        return value == null ? null : value.value();
    }
}
