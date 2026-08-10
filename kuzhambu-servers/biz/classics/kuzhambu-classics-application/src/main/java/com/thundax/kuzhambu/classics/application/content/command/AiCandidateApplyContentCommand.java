package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;

public record AiCandidateApplyContentCommand(
        Long candidateId,
        ClassicsContentType contentType,
        Long contentId,
        Long objectId,
        String capability,
        String resultFormat,
        String resultPayload,
        String changeSummary,
        String tagApplyMode) {}
