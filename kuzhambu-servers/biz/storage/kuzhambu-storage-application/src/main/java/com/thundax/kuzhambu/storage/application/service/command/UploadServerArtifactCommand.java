package com.thundax.kuzhambu.storage.application.service.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadServerArtifactCommand {

    private byte[] contentBytes;
    private String originalFilename;
    private String contentType;
    private Long sizeBytes;
}
