package com.thundax.kuzhambu.classics.application.wangqi.result;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import lombok.Getter;

@Getter
public class WangqiDocumentSourceFile {
    private final Long documentId;
    private final Long storageObjectId;
    private final String originalFilename;
    private final String contentType;
    private final Long size;

    public WangqiDocumentSourceFile(Long documentId, StoredObject storage) {
        this.documentId = documentId;
        this.storageObjectId = storage == null || storage.getId() == null
                ? null
                : storage.getId().value();
        this.originalFilename = storage == null ? null : storage.getOriginalFilename();
        this.contentType = storage == null ? null : storage.getContentType();
        this.size = storage == null ? null : storage.getSize();
    }
}
