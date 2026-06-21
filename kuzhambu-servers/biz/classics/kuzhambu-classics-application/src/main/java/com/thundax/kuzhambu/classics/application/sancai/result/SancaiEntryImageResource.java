package com.thundax.kuzhambu.classics.application.sancai.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiEntryImageResource {
    private Long entryId;
    private Long imageId;
    private Long storageObjectId;
    private String originalFilename;
    private String contentType;
    private Long size;
    private String previewUrl;
    private String downloadUrl;
}
