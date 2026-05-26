package com.thundax.kuzhambu.biz.storage.entity;

import com.thundax.kuzhambu.biz.storage.entity.enums.StorageOwnerType;
import com.thundax.kuzhambu.biz.storage.entity.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.biz.storage.entity.enums.StoredObjectStatus;
import com.thundax.kuzhambu.biz.storage.entity.valueobject.StoredObjectId;
import com.thundax.kuzhambu.biz.storage.entity.valueobject.StoredObjectIdCodec;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

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
        return StringUtils.isBlank(originalFilename) ? getOriginalFileName() : originalFilename;
    }

    public String getContentType() {
        return StringUtils.isBlank(contentType) ? mimeType : contentType;
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
        if (StringUtils.isNotBlank(originalFilename)) {
            return originalFilename;
        }
        if (StringUtils.isBlank(this.getExtendName())) {
            return this.getName();
        }
        return this.getName() + DOT + this.getExtendName();
    }

    public String getPathName() {
        return new SimpleDateFormat(PATH_FORMAT).format(new Date()) + PATH_SEPARATOR + this.getFileName();
    }
}
