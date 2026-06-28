package com.thundax.kuzhambu.classics.application.result;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClassicsStoredContentResult {
    private final Long storageObjectId;
    private final String originalFilename;
    private final String contentType;
    private final Long size;
    private final InputStream inputStream;
}
