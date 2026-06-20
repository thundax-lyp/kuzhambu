package com.thundax.kuzhambu.classics.application.wangqi.command;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WangqiDocumentSourceFileCommand {
    private Long documentId;
    private InputStream inputStream;
    private String originalFilename;
    private String contentType;
    private long size;
}
