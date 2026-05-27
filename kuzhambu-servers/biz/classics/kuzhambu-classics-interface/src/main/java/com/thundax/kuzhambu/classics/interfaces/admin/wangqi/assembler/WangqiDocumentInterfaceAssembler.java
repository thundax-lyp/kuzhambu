package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.assembler;

import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSaveCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentVisibilityCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.request.WangqiDocumentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentResponse;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import org.apache.commons.lang3.StringUtils;

public final class WangqiDocumentInterfaceAssembler {
    private WangqiDocumentInterfaceAssembler() {}

    public static WangqiDocumentPageQuery toQuery(WangqiDocumentRequest request) {
        return new WangqiDocumentPageQuery(
                request.getKeyword(),
                visibility(request.getVisibility()),
                StringUtils.isBlank(request.getSortDirection())
                        ? SortDirection.ASC
                        : SortDirection.valueOf(
                                request.getSortDirection().trim().toUpperCase()));
    }

    public static WangqiDocumentSaveCommand toSaveCommand(WangqiDocumentRequest request) {
        return new WangqiDocumentSaveCommand(
                request.getId(),
                request.getTitle(),
                request.getSummary(),
                StringUtils.isBlank(request.getContentFormat())
                        ? null
                        : WangqiContentFormat.from(request.getContentFormat()),
                request.getContent(),
                request.getDocumentTime(),
                request.getStorageObjectId(),
                visibility(request.getVisibility()));
    }

    public static WangqiDocumentVisibilityCommand toVisibilityCommand(WangqiDocumentRequest request) {
        return new WangqiDocumentVisibilityCommand(request.getId(), visibility(request.getVisibility()));
    }

    public static WangqiDocumentResponse toResponse(WangqiDocument entity) {
        return entity == null
                ? WangqiDocumentResponse.builder().build()
                : WangqiDocumentResponse.builder()
                        .id(entity.getId())
                        .title(entity.getTitle())
                        .summary(entity.getSummary())
                        .contentFormat(
                                entity.getContentFormat() == null
                                        ? null
                                        : entity.getContentFormat().value())
                        .content(entity.getContent())
                        .documentTime(entity.getDocumentTime())
                        .storageObjectId(entity.getStorageObjectId())
                        .visibility(
                                entity.getVisibility() == null
                                        ? null
                                        : entity.getVisibility().value())
                        .build();
    }

    private static WangqiDocumentVisibility visibility(String value) {
        return StringUtils.isBlank(value) ? null : WangqiDocumentVisibility.from(value);
    }
}
