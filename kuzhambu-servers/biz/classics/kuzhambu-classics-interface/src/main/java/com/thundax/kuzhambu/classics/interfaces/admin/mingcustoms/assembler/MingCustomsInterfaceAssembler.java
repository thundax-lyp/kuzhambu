package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.assembler;

import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsTagCloudItem;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsKeywordCloudItemResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsTagCloudItemResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsVersionResponse;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import org.apache.commons.lang3.StringUtils;

public final class MingCustomsInterfaceAssembler {
    private MingCustomsInterfaceAssembler() {}

    public static MingCustomsPageQuery toQuery(MingCustomsRequest request) {
        return new MingCustomsPageQuery(
                request.getCategory(),
                request.getKeyword(),
                request.getTagName(),
                request.getTagId(),
                request.getTagNameSnapshot(),
                sortDirection(request.getSortDirection()),
                null);
    }

    public static MingCustomsPageQuery toTagCloudQuery(String category, String keyword) {
        return new MingCustomsPageQuery(category, keyword, null, null, null, null, null);
    }

    public static MingCustomsCommand toCommand(MingCustomsRequest request) {
        return new MingCustomsCommand(
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
                request.getOriginalExcerpts());
    }

    public static MingCustomsKeywordCommand toKeywordCommand(Long customId, MingCustomsRequest request) {
        return new MingCustomsKeywordCommand(MingCustomsEntryIdCodec.toDomain(customId), request.getKeyword());
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
                        .lifecycleStatus(
                                entity.getLifecycleStatus() == null
                                        ? null
                                        : entity.getLifecycleStatus().name())
                        .transitionStatus(
                                entity.getTransitionStatus() == null
                                        ? null
                                        : entity.getTransitionStatus().name())
                        .currentPublicationJobId(
                                entity.getCurrentPublicationJobId() == null
                                        ? null
                                        : entity.getCurrentPublicationJobId().value())
                        .build();
    }

    public static MingCustomsKeywordCloudItemResponse toKeywordCloudResponse(MingCustomsKeywordCloudItem item) {
        return item == null
                ? MingCustomsKeywordCloudItemResponse.builder().build()
                : MingCustomsKeywordCloudItemResponse.builder()
                        .keyword(item.getKeyword())
                        .count(item.getCount())
                        .build();
    }

    public static MingCustomsTagCloudItemResponse toTagCloudResponse(MingCustomsTagCloudItem item) {
        return item == null
                ? MingCustomsTagCloudItemResponse.builder().build()
                : MingCustomsTagCloudItemResponse.builder()
                        .tagId(item.getTagId())
                        .tagNameSnapshot(item.getTagNameSnapshot())
                        .count(item.getCount())
                        .build();
    }

    public static MingCustomsVersionResponse toVersionResponse(ClassicsContentVersion version) {
        return version == null
                ? MingCustomsVersionResponse.builder().build()
                : MingCustomsVersionResponse.builder()
                        .id(ClassicsContentVersionIdCodec.toValue(version.getId()))
                        .contentType(
                                version.getContentType() == null
                                        ? null
                                        : version.getContentType().value())
                        .contentId(ClassicsContentIdCodec.toValue(version.getContentId()))
                        .versionNo(version.getVersionNo())
                        .versionedAt(version.getVersionedAt())
                        .snapshotJson(version.getSnapshotJson())
                        .changeType(
                                version.getChangeType() == null
                                        ? null
                                        : version.getChangeType().value())
                        .changeSummary(version.getChangeSummary())
                        .build();
    }

    private static SortDirection sortDirection(String value) {
        return StringUtils.isBlank(value)
                ? SortDirection.ASC
                : SortDirection.valueOf(value.trim().toUpperCase());
    }
}
