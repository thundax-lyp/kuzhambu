package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;

public record ContentTagCommand(
        Long id,
        ClassicsContentType contentType,
        Long contentId,
        Long tagId,
        String tagNameSnapshot,
        ClassicsContentSource source,
        ClassicsContentTagStatus status) {}
