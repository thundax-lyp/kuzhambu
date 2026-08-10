package com.thundax.kuzhambu.classics.application.wangqi.command;

import java.io.InputStream;

public record WangqiDocumentSourceFileCommand(
        Long documentId, InputStream inputStream, String originalFilename, String contentType, long size) {}
