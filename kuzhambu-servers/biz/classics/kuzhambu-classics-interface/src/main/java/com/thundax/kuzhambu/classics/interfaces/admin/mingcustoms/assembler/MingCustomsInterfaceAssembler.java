package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.assembler;

import com.thundax.kuzhambu.classics.application.content.query.ContentObjectQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordSortCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsQuery;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsKeywordIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsTagCloudItem;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsKeywordSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsKeywordCloudItemResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsTagCloudItemResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsVersionResponse;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class MingCustomsInterfaceAssembler {
    private MingCustomsInterfaceAssembler() {}

    @NonNull
    public static ContentObjectQuery toContentObjectQuery(@NonNull String contentType, @NonNull Long contentId) {
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(contentId, "contentId");
        return new ContentObjectQuery(contentType, ClassicsContentIdCodec.toDomain(contentId));
    }

    @NonNull
    public static MingCustomsQuery toQuery(@NonNull MingCustomsRequest request) {
        Objects.requireNonNull(request, "request");
        return toQuery(request, Set.of());
    }

    @NonNull
    public static MingCustomsQuery toQuery(
            @NonNull MingCustomsRequest request, @NonNull Set<String> operatorPermissions) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(operatorPermissions, "operatorPermissions");
        return new MingCustomsQuery(
                request.getCategory(),
                request.getKeyword(),
                request.getTagName(),
                request.getTagId(),
                request.getTagNameSnapshot(),
                sortDirection(request.getSortDirection()),
                operatorPermissions);
    }

    @NonNull
    public static MingCustomsQuery toTagCloudQuery(@NonNull String category, @NonNull String keyword) {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(keyword, "keyword");
        return new MingCustomsQuery(category, keyword, null, null, null, null, null);
    }

    @NonNull
    public static MingCustomsQuery toTagCloudQuery(
            @NonNull MingCustomsRequest request, @NonNull Set<String> operatorPermissions) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(operatorPermissions, "operatorPermissions");
        return new MingCustomsQuery(
                request.getCategory(), request.getKeyword(), null, null, null, null, operatorPermissions);
    }

    @NonNull
    public static MingCustomsCommand toCommand(@NonNull MingCustomsRequest request) {
        Objects.requireNonNull(request, "request");
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

    @NonNull
    public static MingCustomsKeywordCommand toKeywordCommand(
            @NonNull Long customId, @NonNull MingCustomsRequest request) {
        Objects.requireNonNull(customId, "customId");
        Objects.requireNonNull(request, "request");
        return new MingCustomsKeywordCommand(MingCustomsEntryIdCodec.toDomain(customId), request.getKeyword());
    }

    @NonNull
    public static MingCustomsKeywordSortCommand toKeywordSortCommand(@NonNull MingCustomsKeywordSortRequest request) {
        Objects.requireNonNull(request, "request");
        return new MingCustomsKeywordSortCommand(RequestListHelper.map(
                RequestListHelper.presentUnique(
                        request.getOrderedIds(), "orderedIds", AdminResponseExceptions::invalidParameter),
                MingCustomsKeywordIdCodec::toDomain));
    }

    @NonNull
    public static MingCustomsResponse toResponse(@NonNull MingCustomsEntry entity) {
        Objects.requireNonNull(entity, "entity");
        return MingCustomsResponse.builder()
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

    @NonNull
    public static MingCustomsKeywordCloudItemResponse toKeywordCloudResponse(
            @NonNull MingCustomsKeywordCloudItem item) {
        Objects.requireNonNull(item, "item");
        return MingCustomsKeywordCloudItemResponse.builder()
                .keyword(item.getKeyword())
                .count(item.getCount())
                .build();
    }

    @NonNull
    public static MingCustomsTagCloudItemResponse toTagCloudResponse(@NonNull MingCustomsTagCloudItem item) {
        Objects.requireNonNull(item, "item");
        return MingCustomsTagCloudItemResponse.builder()
                .tagId(item.getTagId())
                .tagNameSnapshot(item.getTagNameSnapshot())
                .count(item.getCount())
                .build();
    }

    @NonNull
    public static MingCustomsVersionResponse toVersionResponse(@NonNull ClassicsContentVersion version) {
        Objects.requireNonNull(version, "version");
        return MingCustomsVersionResponse.builder()
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
