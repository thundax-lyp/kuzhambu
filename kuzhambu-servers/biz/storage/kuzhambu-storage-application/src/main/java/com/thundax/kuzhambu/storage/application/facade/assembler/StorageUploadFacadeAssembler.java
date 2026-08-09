package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.command.AbortMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.CompleteMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.InitMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.UploadMultipartPartCommand;
import com.thundax.kuzhambu.storage.application.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartNumber;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.facade.request.AbortMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.CompleteMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.InitMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadMultipartPartFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.AbortMultipartUploadFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.CompleteMultipartUploadFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.InitMultipartUploadFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadMultipartPartFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StorageUploadFacadeAssembler {

    @NonNull
    public UploadStorageObjectCommand toUploadStorageObjectCommand(@NonNull UploadStorageFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new UploadStorageObjectCommand(
                request.getInputStream(),
                request.getOriginalFilename(),
                request.getContentType(),
                toStorageByteSize(request.getSizeBytes()),
                request.getAllowedSuffixes(),
                StorageOwnerRef.ofNullable(toOwnerType(request), request.getOwnerId()),
                toObjectStatus(request),
                toReferenceStatus(request),
                request.getRemarks());
    }

    @NonNull
    public InitMultipartUploadCommand toInitMultipartUploadCommand(@NonNull InitMultipartUploadFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new InitMultipartUploadCommand(
                toMultipartUploadId(request.getUploadId()),
                StorageOwnerRef.ofNullable(toOwnerType(request.getOwnerType()), request.getOwnerId()),
                request.getBusinessType(),
                request.getOriginalFilename(),
                StringUtils.isBlank(request.getMimeType()) ? null : new StorageMimeType(request.getMimeType()),
                null,
                null,
                null,
                toStorageByteSize(request.getTotalSize()),
                toMultipartPartSize(request.getPartSize()));
    }

    @NonNull
    public UploadMultipartPartCommand toUploadMultipartPartCommand(@NonNull UploadMultipartPartFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new UploadMultipartPartCommand(
                toMultipartUploadId(request.getUploadId()),
                toMultipartPartNumber(request.getPartNumber()),
                request.getEtag(),
                toStorageByteSize(request.getSize()),
                request.getInputStream());
    }

    @NonNull
    public CompleteMultipartUploadCommand toCompleteMultipartUploadCommand(
            @NonNull CompleteMultipartUploadFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new CompleteMultipartUploadCommand(toMultipartUploadId(request.getUploadId()), null, null, null, null);
    }

    @NonNull
    public AbortMultipartUploadCommand toAbortMultipartUploadCommand(
            @NonNull AbortMultipartUploadFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new AbortMultipartUploadCommand(toMultipartUploadId(request.getUploadId()));
    }

    @NonNull
    public InitMultipartUploadFacadeResponse toResponse(@NonNull MultipartUploadSession session) {
        Objects.requireNonNull(session, "session must not be null");
        return InitMultipartUploadFacadeResponse.builder()
                .uploadId(session.getUploadId())
                .providerUploadId(session.getProviderUploadId())
                .ownerType(valueOf(session.getOwnerType()))
                .ownerId(session.getOwnerId())
                .businessType(session.getBusinessType())
                .originalFilename(session.getOriginalFilename())
                .mimeType(session.getMimeType())
                .bucketName(session.getBucketName())
                .objectKey(session.getObjectKey())
                .totalSize(session.getTotalSize())
                .partSize(session.getPartSize())
                .uploadedPartCount(session.getUploadedPartCount())
                .uploadStatus(valueOf(session.getUploadStatus()))
                .build();
    }

    @NonNull
    public UploadMultipartPartFacadeResponse toResponse(
            @NonNull com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart part) {
        Objects.requireNonNull(part, "part must not be null");
        return UploadMultipartPartFacadeResponse.builder()
                .uploadId(part.getUploadId())
                .partNumber(part.getPartNumber())
                .etag(part.getEtag())
                .size(part.getSize())
                .build();
    }

    @NonNull
    public CompleteMultipartUploadFacadeResponse toResponse(
            @NonNull StoredObject storage, @NonNull CompleteMultipartUploadFacadeRequest request) {
        Objects.requireNonNull(storage, "storage must not be null");
        Objects.requireNonNull(request, "request must not be null");
        return CompleteMultipartUploadFacadeResponse.builder()
                .storageObjectId(
                        storage.getId() == null ? null : storage.getId().value())
                .uploadId(request.getUploadId())
                .businessType(null)
                .originalFilename(storage.getOriginalFilename())
                .mimeType(storage.getMimeType())
                .bucketName(storage.getBucketName())
                .objectKey(storage.getObjectKey())
                .size(storage.getSize())
                .accessEndpoint(storage.getAccessEndpoint())
                .objectStatus(valueOf(storage.getObjectStatus()))
                .referenceStatus(valueOf(storage.getReferenceStatus()))
                .providerUploadId(null)
                .build();
    }

    @NonNull
    public AbortMultipartUploadFacadeResponse toResponse(@NonNull AbortMultipartUploadFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return AbortMultipartUploadFacadeResponse.builder()
                .uploadId(request.getUploadId())
                .uploadStatus(MultipartUploadStatus.ABORTED.value())
                .build();
    }

    private StorageOwnerType toOwnerType(UploadStorageFacadeRequest request) {
        return StringUtils.isBlank(request.getOwnerType()) ? null : toOwnerType(request.getOwnerType());
    }

    private StoredObjectStatus toObjectStatus(UploadStorageFacadeRequest request) {
        return StringUtils.isBlank(request.getObjectStatus())
                ? null
                : StoredObjectStatus.from(request.getObjectStatus());
    }

    private StoredObjectReferenceStatus toReferenceStatus(UploadStorageFacadeRequest request) {
        return StringUtils.isBlank(request.getReferenceStatus())
                ? null
                : StoredObjectReferenceStatus.from(request.getReferenceStatus());
    }

    private StorageOwnerType toOwnerType(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return StorageOwnerType.from(value);
    }

    private String valueOf(StorageOwnerType value) {
        return value == null ? null : value.value();
    }

    private String valueOf(StoredObjectStatus value) {
        return value == null ? null : value.value();
    }

    private String valueOf(StoredObjectReferenceStatus value) {
        return value == null ? null : value.value();
    }

    private String valueOf(MultipartUploadStatus value) {
        return value == null ? null : value.value();
    }

    private MultipartUploadId toMultipartUploadId(String value) {
        return StringUtils.isBlank(value) ? null : toBizValueObject(() -> new MultipartUploadId(value));
    }

    private MultipartPartNumber toMultipartPartNumber(Integer value) {
        return value == null ? null : toBizValueObject(() -> new MultipartPartNumber(value));
    }

    private StorageByteSize toStorageByteSize(Long value) {
        return value == null ? null : toBizValueObject(() -> new StorageByteSize(value));
    }

    private MultipartPartSize toMultipartPartSize(Long value) {
        return value == null ? null : toBizValueObject(() -> new MultipartPartSize(value));
    }

    private <T> T toBizValueObject(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (IllegalArgumentException exception) {
            throw new BizException(exception.getMessage());
        }
    }

    @NonNull
    public UploadStorageFacadeResponse toResponse(@NonNull StoredObject storage) {
        Objects.requireNonNull(storage, "storage must not be null");
        return UploadStorageFacadeResponse.builder()
                .storageObjectId(
                        storage.getId() == null ? null : storage.getId().value())
                .originalFilename(storage.getOriginalFilename())
                .contentType(storage.getContentType())
                .name(storage.getName())
                .extendName(storage.getExtendName())
                .mimeType(storage.getMimeType())
                .bucketName(storage.getBucketName())
                .objectKey(storage.getObjectKey())
                .sizeBytes(storage.getSize())
                .accessEndpoint(storage.getAccessEndpoint())
                .objectStatus(
                        storage.getObjectStatus() == null
                                ? null
                                : storage.getObjectStatus().value())
                .referenceStatus(
                        storage.getReferenceStatus() == null
                                ? null
                                : storage.getReferenceStatus().value())
                .remarks(storage.getRemarks())
                .build();
    }
}
