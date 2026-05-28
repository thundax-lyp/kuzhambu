package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.assembler;

import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsSaveCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsResponse;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import org.apache.commons.lang3.StringUtils;

public final class MingCustomsInterfaceAssembler {
    private MingCustomsInterfaceAssembler() {}

    public static MingCustomsPageQuery toQuery(MingCustomsRequest request) {
        return new MingCustomsPageQuery(
                request.getCategory(),
                request.getKeyword(),
                request.getTagName(),
                visibility(request.getVisibility()),
                StringUtils.isBlank(request.getSortDirection())
                        ? SortDirection.ASC
                        : SortDirection.valueOf(
                                request.getSortDirection().trim().toUpperCase()));
    }

    public static MingCustomsSaveCommand toSaveCommand(MingCustomsRequest request) {
        return new MingCustomsSaveCommand(
                MingCustomsEntryIdCodec.toDomain(request.getId()),
                request.getTitle(),
                request.getCategory(),
                request.getChapter(),
                request.getSection(),
                request.getSummary(),
                StringUtils.isBlank(request.getContentFormat())
                        ? null
                        : MingCustomsContentFormat.from(request.getContentFormat()),
                request.getContent(),
                request.getOriginalExcerpts(),
                visibility(request.getVisibility()));
    }

    public static MingCustomsKeywordCommand toKeywordCommand(Long customId, MingCustomsRequest request) {
        return new MingCustomsKeywordCommand(
                MingCustomsEntryIdCodec.toDomain(customId), request.getKeyword(), request.getPriority());
    }

    public static MingCustomsResponse toResponse(MingCustomsEntry entity) {
        return entity == null
                ? MingCustomsResponse.builder().build()
                : MingCustomsResponse.builder()
                        .id(entity.getId() == null ? null : entity.getId().value())
                        .title(entity.getTitle())
                        .category(entity.getCategory())
                        .chapter(entity.getChapter())
                        .section(entity.getSection())
                        .summary(entity.getSummary())
                        .contentFormat(
                                entity.getContentFormat() == null
                                        ? null
                                        : entity.getContentFormat().value())
                        .content(entity.getContent())
                        .originalExcerpts(entity.getOriginalExcerpts())
                        .visibility(
                                entity.getVisibility() == null
                                        ? null
                                        : entity.getVisibility().value())
                        .build();
    }

    private static MingCustomsVisibility visibility(String value) {
        return StringUtils.isBlank(value) ? null : MingCustomsVisibility.from(value);
    }
}
