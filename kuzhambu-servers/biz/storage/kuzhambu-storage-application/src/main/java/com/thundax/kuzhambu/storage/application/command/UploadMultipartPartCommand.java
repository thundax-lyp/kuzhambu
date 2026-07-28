package com.thundax.kuzhambu.storage.application.command;

import java.io.InputStream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UploadMultipartPartCommand {
    private String uploadId;
    private Integer partNumber;
    private String etag;
    private Long size;
    private InputStream inputStream;

    public UploadMultipartPartCommand(
            String uploadId, Integer partNumber, String etag, Long size, InputStream inputStream) {
        this.uploadId = uploadId;
        this.partNumber = partNumber;
        this.etag = etag;
        this.size = size;
        this.inputStream = inputStream;
    }

    public UploadMultipartPartCommand(String uploadId, Integer partNumber, String etag, Long size) {
        this(uploadId, partNumber, etag, size, null);
    }
}
