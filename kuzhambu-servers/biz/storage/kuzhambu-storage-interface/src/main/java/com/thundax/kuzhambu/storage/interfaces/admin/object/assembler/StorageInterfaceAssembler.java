package com.thundax.kuzhambu.storage.interfaces.admin.object.assembler;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.StoragePageRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.AbortMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.CompleteMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.InitMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.StorageObjectResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.UploadMultipartPartResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class StorageInterfaceAssembler {

    private StorageInterfaceAssembler() {}

    @NonNull
    public static StorageQuery toQuery(@NonNull StoragePageRequest request) {
        StorageQuery query = new StorageQuery();
        query.setContentType(emptyToNull(request.getContentType()));
        query.setReferenceOwnerId(emptyToNull(request.getReferenceOwnerId()));
        query.setReferenceOwnerType(emptyToNull(request.getReferenceOwnerType()));
        query.setObjectStatus(objectStatusFrom(request.getObjectStatus()));
        query.setReferenceStatus(referenceStatusFrom(request.getReferenceStatus()));
        query.setOriginalFilename(emptyToNull(request.getOriginalFilename()));
        query.setRemarks(emptyToNull(request.getRemarks()));
        query.setSortDirection(sortDirectionFrom(request.getSortDirection()));
        return query;
    }

    @NonNull
    public static StorageObjectResponse toResponse(StoredObject entity) {
        if (entity == null) {
            return StorageObjectResponse.builder().build();
        }
        return StorageObjectResponse.builder()
                .id(StoredObjectIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .originalFilename(entity.getOriginalFilename())
                .contentType(entity.getContentType())
                .size(entity.getSize())
                .accessEndpoint(entity.getAccessEndpoint())
                .objectStatus(objectStatusValue(entity.getObjectStatus()))
                .referenceStatus(referenceStatusValue(entity.getReferenceStatus()))
                .build();
    }

    @NonNull
    public static InitMultipartUploadResponse toResponse(MultipartUploadSession session) {
        return session == null
                ? InitMultipartUploadResponse.builder().build()
                : InitMultipartUploadResponse.builder()
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
    public static UploadMultipartPartResponse toResponse(MultipartUploadPart part) {
        return part == null
                ? UploadMultipartPartResponse.builder().build()
                : UploadMultipartPartResponse.builder()
                        .uploadId(part.getUploadId())
                        .partNumber(part.getPartNumber())
                        .etag(part.getEtag())
                        .size(part.getSize())
                        .build();
    }

    @NonNull
    public static CompleteMultipartUploadResponse toResponse(StoredObject storage, String uploadId) {
        return storage == null
                ? CompleteMultipartUploadResponse.builder().build()
                : CompleteMultipartUploadResponse.builder()
                        .id(StoredObjectIdCodec.toStringValue(storage.getId()))
                        .uploadId(uploadId)
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
    public static AbortMultipartUploadResponse toResponse(String uploadId) {
        return AbortMultipartUploadResponse.builder()
                .uploadId(uploadId)
                .uploadStatus(MultipartUploadStatus.ABORTED.value())
                .build();
    }

    private static StoredObjectStatus objectStatusFrom(String value) {
        return StringUtils.isBlank(value) ? null : StoredObjectStatus.from(value);
    }

    private static StoredObjectReferenceStatus referenceStatusFrom(String value) {
        return StringUtils.isBlank(value) ? null : StoredObjectReferenceStatus.from(value);
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
