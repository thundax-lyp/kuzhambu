package com.thundax.kuzhambu.classics.application.wangqi.result;

import lombok.Getter;

@Getter
public class WangqiDocumentSourceFile {
    private final Long documentId;
    private final Long storageObjectId;
    private final String originalFilename;
    private final String contentType;
    private final Long size;

    public WangqiDocumentSourceFile(
            Long documentId, Long storageObjectId, String originalFilename, String contentType, Long size) {
        this.documentId = documentId;
        this.storageObjectId = storageObjectId;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
    }
}
