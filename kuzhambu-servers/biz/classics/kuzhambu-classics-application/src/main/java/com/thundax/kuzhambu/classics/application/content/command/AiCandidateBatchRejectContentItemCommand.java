package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;

public record AiCandidateBatchRejectContentItemCommand(
        Long candidateId, ClassicsContentType contentType, Long contentId, Long objectId, String capability) {}
