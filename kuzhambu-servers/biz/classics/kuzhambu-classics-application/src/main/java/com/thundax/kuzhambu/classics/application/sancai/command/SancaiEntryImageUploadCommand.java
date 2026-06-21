package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiEntryImageUploadCommand {
    private Long entryId;
    private InputStream inputStream;
    private String originalFilename;
    private String contentType;
    private long size;
    private String title;
    private SancaiEntryImageType imageType;
    private boolean currentUsed;
    private Long replaceImageId;
}
