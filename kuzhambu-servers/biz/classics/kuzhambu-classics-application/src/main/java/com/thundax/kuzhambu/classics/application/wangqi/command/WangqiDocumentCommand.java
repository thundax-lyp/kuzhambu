package com.thundax.kuzhambu.classics.application.wangqi.command;

import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import java.time.Instant;

public record WangqiDocumentCommand(
        Long id,
        String title,
        String summary,
        WangqiContentFormat contentFormat,
        String content,
        Instant documentTime,
        Long storageObjectId) {}
