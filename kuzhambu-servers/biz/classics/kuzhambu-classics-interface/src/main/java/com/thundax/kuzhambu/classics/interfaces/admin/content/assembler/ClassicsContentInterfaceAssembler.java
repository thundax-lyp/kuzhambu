package com.thundax.kuzhambu.classics.interfaces.admin.content.assembler;

import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportFormat;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportKind;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportScopeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import org.apache.commons.lang3.StringUtils;

public final class ClassicsContentInterfaceAssembler {
    private ClassicsContentInterfaceAssembler() {}

    public static ContentTagCommand toTagCommand(ClassicsContentRequest request) {
        return new ContentTagCommand(
                request.getId(),
                type(request.getContentType()),
                request.getContentId(),
                request.getTagId(),
                request.getTagNameSnapshot(),
                source(request.getSource()),
                StringUtils.isBlank(request.getStatus())
                        ? ClassicsContentTagStatus.ACTIVE
                        : ClassicsContentTagStatus.from(request.getStatus()),
                request.getPriority());
    }

    public static ContentQaPairCommand toQaCommand(ClassicsContentRequest request) {
        return new ContentQaPairCommand(
                request.getId(),
                type(request.getContentType()),
                request.getContentId(),
                request.getQuestion(),
                request.getAnswer(),
                source(request.getSource()),
                request.getPriority());
    }

    public static ContentExportCommand toExportCommand(ClassicsContentRequest request) {
        return new ContentExportCommand(
                ClassicsExportKind.from(request.getExportKind()),
                type(request.getContentType()),
                ClassicsExportFormat.from(request.getExportFormat()),
                ClassicsExportScopeType.from(request.getScopeType()),
                request.getScopeJson(),
                null,
                request.getExpiresAt(),
                ClassicsExportStatus.REQUESTED,
                null,
                0,
                0,
                null,
                false);
    }

    public static ClassicsContentResponse toTagResponse(ClassicsContentTag tag) {
        return tag == null
                ? ClassicsContentResponse.builder().build()
                : ClassicsContentResponse.builder()
                        .id(tag.getId())
                        .contentType(
                                tag.getContentType() == null
                                        ? null
                                        : tag.getContentType().value())
                        .contentId(tag.getContentId())
                        .tagNameSnapshot(tag.getTagNameSnapshot())
                        .status(tag.getStatus() == null ? null : tag.getStatus().value())
                        .build();
    }

    public static ClassicsContentResponse toQaResponse(ClassicsContentQaPair qa) {
        return qa == null
                ? ClassicsContentResponse.builder().build()
                : ClassicsContentResponse.builder()
                        .id(qa.getId())
                        .contentType(
                                qa.getContentType() == null
                                        ? null
                                        : qa.getContentType().value())
                        .contentId(qa.getContentId())
                        .question(qa.getQuestion())
                        .answer(qa.getAnswer())
                        .build();
    }

    private static ClassicsContentType type(String value) {
        return StringUtils.isBlank(value) ? null : ClassicsContentType.from(value);
    }

    private static ClassicsContentSource source(String value) {
        return StringUtils.isBlank(value) ? ClassicsContentSource.MANUAL : ClassicsContentSource.from(value);
    }
}
