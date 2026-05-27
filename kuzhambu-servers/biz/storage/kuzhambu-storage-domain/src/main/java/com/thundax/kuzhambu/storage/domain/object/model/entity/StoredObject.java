package com.thundax.kuzhambu.storage.domain.object.model.entity;

import com.thundax.kuzhambu.common.core.sort.Sortable;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoredObject implements Sortable {
    private static final String PATH_FORMAT = "yyyyMM";
    private static final String DOT = ".";
    private static final String PATH_SEPARATOR = "/";

    private StoredObjectId id;
    private String originalFilename;
    private String contentType;
    private String name;
    private String extendName;
    private String mimeType;
    private String ownerId;
    private StorageOwnerType ownerType;
    private String bucketName;
    private String objectKey;
    private Long size;
    private String accessEndpoint;
    private StoredObjectStatus objectStatus = StoredObjectStatus.ACTIVE;
    private StoredObjectReferenceStatus referenceStatus = StoredObjectReferenceStatus.UNREFERENCED;
    private int priority;
    private String remarks;

    public String getOriginalFilename() {
        return isBlank(originalFilename) ? getOriginalFileName() : originalFilename;
    }

    public String getContentType() {
        return isBlank(contentType) ? mimeType : contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        this.mimeType = contentType;
    }

    public boolean isEnable() {
        return StoredObjectStatus.ACTIVE == getObjectStatus();
    }

    public String getFileName() {
        return StoredObjectIdCodec.toValue(getId()) + DOT + this.getExtendName();
    }

    public String getOriginalFileName() {
        if (isNotBlank(originalFilename)) {
            return originalFilename;
        }
        if (isBlank(this.getExtendName())) {
            return this.getName();
        }
        return this.getName() + DOT + this.getExtendName();
    }

    public String getPathName() {
        return new SimpleDateFormat(PATH_FORMAT).format(new Date()) + PATH_SEPARATOR + this.getFileName();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isNotBlank(String value) {
        return !isBlank(value);
    }
}
