package com.thundax.kuzhambu.storage.infra.object.persistence.assembler;

import com.thundax.kuzhambu.storage.domain.object.codec.MultipartPartNumberCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.MultipartPartSizeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.MultipartUploadPartIdCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.MultipartUploadSessionIdCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageBucketNameCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageByteSizeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageMimeTypeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageObjectKeyCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageOwnerParamsCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.MultipartUploadPartDO;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.MultipartUploadSessionDO;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.StoredObjectDO;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.StoredObjectReferenceDO;
import java.util.ArrayList;
import java.util.List;

public final class StoragePersistenceAssembler {

    private StoragePersistenceAssembler() {}

    public static StoredObjectDO toObject(StoredObject entity) {
        if (entity == null) {
            return null;
        }
        StoredObjectDO dataObject = new StoredObjectDO();
        dataObject.setId(StoredObjectIdCodec.toValue(entity.getId()));
        dataObject.setName(entity.getName());
        dataObject.setExtendName(entity.getExtendName());
        dataObject.setMimeType(StorageMimeTypeCodec.toValue(entity.getMimeTypeRef()));
        dataObject.setBucketName(StorageBucketNameCodec.toValue(entity.getBucketNameRef()));
        dataObject.setObjectKey(StorageObjectKeyCodec.toValue(entity.getObjectKeyRef()));
        dataObject.setSize(StorageByteSizeCodec.toValue(entity.getSizeRef()));
        dataObject.setAccessEndpoint(entity.getAccessEndpoint());
        dataObject.setStoredAt(entity.getStoredAt());
        dataObject.setObjectStatus(statusValue(entity.getObjectStatus()));
        dataObject.setReferenceStatus(referenceStatusValue(entity.getReferenceStatus()));
        dataObject.setPriority(priorityOrDefault(entity.getPriority()));
        dataObject.setRemarks(entity.getRemarks());
        return dataObject;
    }

    public static StoredObject toDomain(StoredObjectDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        StoredObject entity = new StoredObject();
        entity.setId(StoredObjectIdCodec.toDomain(dataObject.getId()));
        entity.setName(dataObject.getName());
        entity.setExtendName(dataObject.getExtendName());
        entity.setMimeTypeRef(StorageMimeTypeCodec.toDomain(dataObject.getMimeType()));
        entity.setBucketNameRef(StorageBucketNameCodec.toDomain(dataObject.getBucketName()));
        entity.setObjectKeyRef(StorageObjectKeyCodec.toDomain(dataObject.getObjectKey()));
        entity.setSizeRef(StorageByteSizeCodec.toDomain(dataObject.getSize()));
        entity.setAccessEndpoint(dataObject.getAccessEndpoint());
        entity.setStoredAt(dataObject.getStoredAt());
        entity.setObjectStatus(statusFrom(dataObject.getObjectStatus()));
        entity.setReferenceStatus(referenceStatusFrom(dataObject.getReferenceStatus()));
        entity.setPriority(priorityOrDefault(dataObject.getPriority()));
        entity.setRemarks(dataObject.getRemarks());
        return entity;
    }

    public static List<StoredObject> toDomainList(List<StoredObjectDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<StoredObject> entities = new ArrayList<>();
        for (StoredObjectDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null || priority < 0 ? 0 : priority;
    }

    private static String ownerTypeValue(StorageOwnerType ownerType) {
        return ownerType == null ? null : ownerType.value();
    }

    private static StorageOwnerType ownerTypeFrom(String ownerType) {
        return ownerType == null ? null : StorageOwnerType.from(ownerType);
    }

    private static String statusValue(StoredObjectStatus status) {
        return status == null ? null : status.value();
    }

    private static StoredObjectStatus statusFrom(String status) {
        return status == null ? null : StoredObjectStatus.from(status);
    }

    private static String referenceStatusValue(StoredObjectReferenceStatus referenceStatus) {
        return referenceStatus == null ? null : referenceStatus.value();
    }

    private static StoredObjectReferenceStatus referenceStatusFrom(String referenceStatus) {
        return referenceStatus == null ? null : StoredObjectReferenceStatus.from(referenceStatus);
    }

    public static StoredObjectReferenceDO toBusinessObject(StoredObjectReference entity) {
        if (entity == null) {
            return null;
        }
        StoredObjectReferenceDO dataObject = new StoredObjectReferenceDO();
        // 复合主键映射：objectId + referenceOwnerType + referenceOwnerId
        dataObject.setObjectId(StoredObjectIdCodec.toValue(entity.getObjectId()));
        dataObject.setReferenceOwnerId(entity.getReferenceOwnerId());
        dataObject.setReferenceOwnerType(entity.getReferenceOwnerType());
        dataObject.setBusinessParams(StorageOwnerParamsCodec.toValue(entity.getOwnerParamsRef()));
        return dataObject;
    }

    public static StoredObjectReference toBusinessDomain(StoredObjectReferenceDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        StoredObjectReference entity = new StoredObjectReference();
        // 按复合主键字段回填引用身份信息
        entity.setObjectId(StoredObjectIdCodec.toDomain(dataObject.getObjectId()));
        entity.setReferenceOwnerId(dataObject.getReferenceOwnerId());
        entity.setReferenceOwnerType(dataObject.getReferenceOwnerType());
        entity.setOwnerParamsRef(StorageOwnerParamsCodec.toDomain(dataObject.getBusinessParams()));
        return entity;
    }

    public static List<StoredObjectReferenceDO> toBusinessObjectList(List<StoredObjectReference> entities) {
        if (entities == null) {
            return null;
        }
        List<StoredObjectReferenceDO> dataObjects = new ArrayList<>();
        for (StoredObjectReference entity : entities) {
            dataObjects.add(toBusinessObject(entity));
        }
        return dataObjects;
    }

    public static List<StoredObjectReference> toBusinessDomainList(List<StoredObjectReferenceDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<StoredObjectReference> entities = new ArrayList<>();
        for (StoredObjectReferenceDO dataObject : dataObjects) {
            entities.add(toBusinessDomain(dataObject));
        }
        return entities;
    }

    public static MultipartUploadSessionDO toMultipartSessionObject(MultipartUploadSession entity) {
        if (entity == null) {
            return null;
        }
        MultipartUploadSessionDO dataObject = new MultipartUploadSessionDO();
        dataObject.setId(MultipartUploadSessionIdCodec.toValue(entity.getId()));
        dataObject.setUploadId(entity.getUploadId());
        dataObject.setOwnerId(entity.getOwnerId());
        dataObject.setOwnerType(ownerTypeValue(entity.getOwnerType()));
        dataObject.setBusinessType(entity.getBusinessType());
        dataObject.setOriginalFilename(entity.getOriginalFilename());
        dataObject.setMimeType(StorageMimeTypeCodec.toValue(entity.getMimeTypeRef()));
        dataObject.setBucketName(StorageBucketNameCodec.toValue(entity.getBucketNameRef()));
        dataObject.setObjectKey(StorageObjectKeyCodec.toValue(entity.getObjectKeyRef()));
        dataObject.setProviderUploadId(entity.getProviderUploadId());
        dataObject.setTotalSize(StorageByteSizeCodec.toValue(entity.getTotalSizeRef()));
        dataObject.setPartSize(MultipartPartSizeCodec.toValue(entity.getPartSizeRef()));
        dataObject.setUploadedPartCount(uploadedPartCountOrDefault(entity.getUploadedPartCount()));
        dataObject.setUploadStatus(uploadStatusValue(entity.getUploadStatus()));
        dataObject.setCompletedDate(entity.getCompletedDate());
        dataObject.setAbortedDate(entity.getAbortedDate());
        return dataObject;
    }

    public static MultipartUploadSession toMultipartSessionDomain(MultipartUploadSessionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MultipartUploadSession entity = new MultipartUploadSession();
        entity.setId(MultipartUploadSessionIdCodec.toDomain(dataObject.getId()));
        entity.setUploadId(dataObject.getUploadId());
        entity.setOwnerId(dataObject.getOwnerId());
        entity.setOwnerType(ownerTypeFrom(dataObject.getOwnerType()));
        entity.setBusinessType(dataObject.getBusinessType());
        entity.setOriginalFilename(dataObject.getOriginalFilename());
        entity.setMimeTypeRef(StorageMimeTypeCodec.toDomain(dataObject.getMimeType()));
        entity.setBucketNameRef(StorageBucketNameCodec.toDomain(dataObject.getBucketName()));
        entity.setObjectKeyRef(StorageObjectKeyCodec.toDomain(dataObject.getObjectKey()));
        entity.setProviderUploadId(dataObject.getProviderUploadId());
        entity.setTotalSizeRef(StorageByteSizeCodec.toDomain(dataObject.getTotalSize()));
        entity.setPartSizeRef(MultipartPartSizeCodec.toDomain(dataObject.getPartSize()));
        entity.setUploadedPartCount(uploadedPartCountOrDefault(dataObject.getUploadedPartCount()));
        entity.setUploadStatus(uploadStatusFrom(dataObject.getUploadStatus()));
        entity.setCompletedDate(dataObject.getCompletedDate());
        entity.setAbortedDate(dataObject.getAbortedDate());
        return entity;
    }

    public static MultipartUploadPartDO toMultipartPartObject(MultipartUploadPart entity) {
        if (entity == null) {
            return null;
        }
        MultipartUploadPartDO dataObject = new MultipartUploadPartDO();
        dataObject.setId(MultipartUploadPartIdCodec.toValue(entity.getId()));
        dataObject.setUploadId(entity.getUploadId());
        dataObject.setPartPath(StorageObjectKeyCodec.toValue(entity.getPartPathRef()));
        dataObject.setPartNumber(MultipartPartNumberCodec.toValue(entity.getPartNumberRef()));
        dataObject.setEtag(entity.getEtag());
        dataObject.setSize(StorageByteSizeCodec.toValue(entity.getSizeRef()));
        return dataObject;
    }

    public static MultipartUploadPart toMultipartPartDomain(MultipartUploadPartDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MultipartUploadPart entity = new MultipartUploadPart();
        entity.setId(MultipartUploadPartIdCodec.toDomain(dataObject.getId()));
        entity.setUploadId(dataObject.getUploadId());
        entity.setPartPathRef(StorageObjectKeyCodec.toDomain(dataObject.getPartPath()));
        entity.setPartNumberRef(MultipartPartNumberCodec.toDomain(dataObject.getPartNumber()));
        entity.setEtag(dataObject.getEtag());
        entity.setSizeRef(StorageByteSizeCodec.toDomain(dataObject.getSize()));
        return entity;
    }

    private static Integer uploadedPartCountOrDefault(Integer uploadedPartCount) {
        return uploadedPartCount == null || uploadedPartCount < 0 ? 0 : uploadedPartCount;
    }

    private static String uploadStatusValue(MultipartUploadStatus uploadStatus) {
        return uploadStatus == null ? null : uploadStatus.value();
    }

    private static MultipartUploadStatus uploadStatusFrom(String uploadStatus) {
        return uploadStatus == null ? null : MultipartUploadStatus.from(uploadStatus);
    }
}
