package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;

public record ContentQaPairCommand(
        Long id,
        ClassicsContentType contentType,
        Long contentId,
        String question,
        String answer,
        ClassicsContentSource source) {}
